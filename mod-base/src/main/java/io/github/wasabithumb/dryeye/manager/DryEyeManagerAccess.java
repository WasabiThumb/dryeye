package io.github.wasabithumb.dryeye.manager;

import io.github.wasabithumb.dryeye.DryEye;
import io.github.wasabithumb.dryeye.util.Pinned;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@NullMarked
@ApiStatus.Internal
final class DryEyeManagerAccess {

    private static final Container CONTAINER = new Container();
    private static final ReadWriteLock TRUTH_LOCK = new ReentrantReadWriteLock();
    private static @Nullable DryEyeManager TRUTH;

    static DryEyeManager get() {
        return CONTAINER.unwrap();
    }

    @Pinned(reason = "used by io.github.wasabithumb.dryeye.DryEye")
    static void register(@Nullable DryEyeManager manager) {
        final Lock l = TRUTH_LOCK.writeLock();
        l.lock();
        try {
            TRUTH = manager;
        } finally {
            l.unlock();
        }
        if (manager == null) {
            CONTAINER.remove();
        } else {
            CONTAINER.set(manager);
        }
    }

    //

    private DryEyeManagerAccess() { }

    //

    private static final class Container extends ThreadLocal<@Nullable DryEyeManager> {

        Container() { }

        //

        public DryEyeManager unwrap() {
            DryEyeManager x = this.get();
            if (x != null) return x;
            final Lock l = TRUTH_LOCK.readLock();
            l.lock();
            try {
                x = TRUTH;
            } finally {
                l.unlock();
            }
            if (x != null) {
                this.set(x);
                return x;
            }
            return new PlaceholderDryEyeManager();
        }

    }

}
