package io.github.wasabithumb.dryeye.app.components;

import io.github.wasabithumb.dryeye.app.context.ApplicationContext;
import io.github.wasabithumb.dryeye.face.Face;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

@NullMarked
public final class FacePanel extends JPanel {

    private static final Instant BLINK_EPOCH = Instant.now();

    //

    private final ApplicationContext ctx;

    public FacePanel(ApplicationContext ctx) {
        super();
        this.ctx = ctx;
    }

    //

    private boolean isBlinking() {
        long time = Duration.between(BLINK_EPOCH, Instant.now()).toMillis();
        long duration = this.ctx.getBlinkDuration();
        long delay = this.ctx.getBlinkDelay();
        long period = duration + delay;
        time = Long.remainderUnsigned(time, period);
        return time < duration;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final int w = this.getWidth();
        final int h = this.getHeight();
        final Face f = this.isBlinking() ?
                this.ctx.getBlinkFace() :
                this.ctx.getFace();

        int sy = 0;
        for (int y = 0; y < 8; y++) {
            int dy = metric(h, y + 1);
            int sx = 0;
            for (int x = 0; x < 8; x++) {
                int dx = metric(w, x + 1);
                g.setColor(getColor(f, x, y));
                g.fillRect(sx, sy, dx - sx, dy - sy);
                sx = dx;
            }
            sy = dy;
        }
    }

    //

    private static java.awt.Color getColor(Face face, int x, int y) {
        return face.get(x, y).toAWT();
    }

    private static int metric(int total, @Range(from = 0, to = 8) int segment) {
        return switch (segment) {
            case 0 -> 0;
            case 1 -> total / 8;
            case 2 -> total / 4;
            case 4 -> total / 2;
            case 8 -> total;
            default -> Math.multiplyExact(segment, total) / 8;
        };
    }

}
