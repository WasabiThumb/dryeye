package io.github.wasabithumb.dryeye;

import io.github.wasabithumb.dryeye.manager.DryEyeManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;

@NullMarked
public abstract class DryEye {

    private static final Method M_REGISTER_MANAGER;
    static {
        try {
            Class<?> cls = Class.forName("io.github.wasabithumb.dryeye.manager.DryEyeManagerAccess");
            Method mRegister = cls.getDeclaredMethod("register", DryEyeManager.class);
            mRegister.setAccessible(true);
            M_REGISTER_MANAGER = mRegister;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve register method", e);
        }
    }

    private static void registerManager(@Nullable DryEyeManager manager) {
        try {
            M_REGISTER_MANAGER.invoke(null, manager);
        } catch (InvocationTargetException | ExceptionInInitializerError e) {
            Throwable cause = e.getCause();
            if (cause == null) cause = e;
            if (cause instanceof RuntimeException re) throw re;
            throw new IllegalStateException("Unexpected checked exception while registering manager", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unexpected reflection error", e);
        }
    }

    private static String elapsed(Instant from, Instant to) {
        Duration dur = Duration.between(from, to);
        if (dur.compareTo(Duration.ofSeconds(10L)) >= 0) {
            return dur.getSeconds() + "s";
        } else if (dur.compareTo(Duration.ofMillis(10L)) >= 0) {
            return dur.toMillis() + "ms";
        } else if (dur.compareTo(Duration.ofNanos(10000L)) >= 0) {
            return (dur.toNanos() / 1000L) + "μs";
        } else {
            return dur.toNanos() + "ns";
        }
    }

    //

    private @UnknownNullability DryEyeManager ownManager;

    //

    @Contract("-> new")
    protected abstract DryEyeManager newManager();

    public void start() {
        final Instant iPreInit = Instant.now();
        final DryEyeManager manager = newManager();
        final Logger logger = manager.logger();
        logger.info("[DryEye] Registering manager");
        registerManager(manager);
        logger.info("[DryEye] Loading config");
        manager.loadConfig();
        final Instant iPostLoad = Instant.now();
        logger.info("[DryEye] Ready in {}", elapsed(iPreInit, iPostLoad));
        this.ownManager = manager;
    }

    public void stop() {
        final DryEyeManager manager = this.ownManager;
        if (manager == null) return;
        final Logger logger = manager.logger();
        logger.info("[DryEye] Unregistering manager");
        registerManager(null);
        this.ownManager = null;
    }

}
