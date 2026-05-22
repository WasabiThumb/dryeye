package io.github.wasabithumb.dryeye.app.context;

import io.github.wasabithumb.dryeye.face.Face;
import io.github.wasabithumb.dryeye.face.VanillaFace;
import io.github.wasabithumb.dryeye.face.WritableFace;
import io.github.wasabithumb.dryeye.face.eye.EyeScheme;
import io.github.wasabithumb.dryeye.face.eye.EyeSchemes;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

@NullMarked
@ApiStatus.Internal
final class ApplicationContextImpl implements ApplicationContext {

    private static final long INITIAL_BLINK_DELAY = 2000;
    private static final long INITIAL_BLINK_DURATION = 50;

    private static Face randomVanillaFace() {
        Face[] values = VanillaFace.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    //

    private final ReadWriteLock lock;
    private final Logger logger;
    private @Nullable Face face;
    private @Nullable EyeScheme eyeScheme;
    private @Nullable Face blinkFace;
    private long blinkDelay;
    private long blinkDuration;

    ApplicationContextImpl(Logger logger) {
        this.lock = new ReentrantReadWriteLock();
        this.logger = logger;
        this.blinkDelay = INITIAL_BLINK_DELAY;
        this.blinkDuration = INITIAL_BLINK_DURATION;
        this.setFace(randomVanillaFace());
    }

    //

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public Face getFace() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            assert this.face != null;
            return this.face;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setFace(Face face) {
        face = Face.copyOf(face);
        EyeScheme scheme = EyeSchemes.match(face);
        Face blinkFace = blinkCopy(face, scheme);

        final Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            this.face = face;
            this.eyeScheme = scheme;
            this.blinkFace = blinkFace;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public EyeScheme getEyeScheme() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            assert this.eyeScheme != null;
            return this.eyeScheme;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setEyeScheme(EyeScheme scheme) {
        final Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            assert this.face != null;
            this.eyeScheme = scheme;
            this.blinkFace = blinkCopy(this.face, scheme);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Face getBlinkFace() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            assert this.blinkFace != null;
            return this.blinkFace;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getBlinkDelay() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return this.blinkDelay;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setBlinkDelay(long period) {
        final Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            this.blinkDelay = Math.max(period, 0);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getBlinkDuration() {
        final Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return this.blinkDuration;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setBlinkDuration(long duration) {
        final Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            this.blinkDuration = Math.max(duration, 1);
        } finally {
            lock.unlock();
        }
    }

    //

    private static Face blinkCopy(Face face, EyeScheme scheme) {
        WritableFace tmp = WritableFace.copyOf(face);
        scheme.blink(tmp);
        return Face.unmodifiableView(tmp);
    }

}
