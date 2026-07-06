package io.github.wasabithumb.dryeye.config;

import io.github.wasabithumb.dryeye.util.MillisRange;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface DryEyeConfig {

    @Contract(pure = true)
    static DryEyeConfig disabled() {
        return DisabledDryEyeConfig.INSTANCE;
    }

    //

    boolean enabled();
    void enabled(boolean enabled);

    MillisRange blinkDelay();
    void blinkDelay(MillisRange blinkDelay);

    MillisRange blinkDuration();
    void blinkDuration(MillisRange blinkDuration);

}
