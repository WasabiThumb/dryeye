package io.github.wasabithumb.dryeye.app;

import io.github.wasabithumb.dryeye.app.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;

public final class Main {

    static void main() {
        // Create application context
        Instant start = Instant.now();
        ApplicationContext ctx = ApplicationContext.create();

        // Set look and feel
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception e) {
            ctx.log(Level.WARNING, "failed to set look and feel", e);
        }

        // Create the main window and loop
        Window window = new Window(ctx);
        TickThread tickThread = new TickThread(ctx, window);
        tickThread.start();

        // Show the window, start loop, establish lifecycle
        SwingUtilities.invokeLater(() -> {
            window.setVisible(true);
            window.setLocationRelativeTo(null);
            tickThread.setState(TickThread.State.TICKING);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    tickThread.setState(TickThread.State.SHUTDOWN);
                }
            });
            Duration elapsed = Duration.between(start, Instant.now());
            ctx.log(Level.INFO, "Ready in " + elapsed.toMillis() + "ms");
        });

        // Wait for window to close
        try {
            tickThread.join();
        } catch (InterruptedException e) {
            ctx.log(Level.WARNING, "main thread interrupted", e);
            System.exit(1);
        }
    }

    //

    private static final class TickThread extends Thread {

        private static Duration calculateTickDuration() {
            long hertz = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDisplayMode()
                    .getRefreshRate();

            // 60 Hz fallback
            if (hertz == DisplayMode.REFRESH_RATE_UNKNOWN)
                hertz = 60;

            return Duration.ofSeconds(1L)
                    .dividedBy(hertz);
        }

        //

        private final Object mutex;
        private final Duration tickDuration;
        private final ApplicationContext ctx;
        private final Window window;
        private State state;

        TickThread(ApplicationContext ctx, Window window) {
            super("DryEye UI Render Thread");
            this.mutex = new Object();
            this.tickDuration = calculateTickDuration();
            this.ctx = ctx;
            this.window = window;
            this.state = State.STALLED;
        }

        //

        public void setState(State state) {
            synchronized (this.mutex) {
                this.state = state;
                this.mutex.notifyAll();
            }
        }

        @Override
        public void run() {
            boolean running;
            do {
                running = tick();
            } while (running);
        }

        private boolean tick() {
            // Check state
            State state;
            synchronized (this.mutex) {
                while ((state = this.state) == State.STALLED) {
                    try {
                        this.mutex.wait();
                    } catch (InterruptedException e) {
                        this.ctx.log(Level.WARNING, "render thread interrupted", e);
                        return false;
                    }
                }
            }
            if (state == State.SHUTDOWN) return false;

            // Render a frame
            final long start = System.nanoTime();
            try {
                this.window.onTick();
            } catch (Exception e) {
                this.ctx.log(Level.SEVERE, "error in tick", e);
            }

            // Implement frame rate limit
            Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
            if (elapsed.compareTo(this.tickDuration) >= 0) return true;
            Duration remainder = this.tickDuration.minus(elapsed);
            LockSupport.parkNanos(remainder.toNanos());
            return !this.isInterrupted();
        }

        //

        private enum State {
            STALLED,
            TICKING,
            SHUTDOWN
        }

    }

}
