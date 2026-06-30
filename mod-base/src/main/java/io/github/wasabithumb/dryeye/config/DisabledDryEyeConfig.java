package io.github.wasabithumb.dryeye.config;

import io.github.wasabithumb.dryeye.util.MillisRange;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ApiStatus.Internal
final class DisabledDryEyeConfig implements DryEyeConfig {

    static final DisabledDryEyeConfig INSTANCE = new DisabledDryEyeConfig();

    //

    private DisabledDryEyeConfig() { }

    //

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public void enabled(boolean enabled) {
        if (enabled) throw new UnsupportedOperationException();
    }

    @Override
    public MillisRange blinkDelay() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void blinkDelay(MillisRange blinkDelay) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MillisRange blinkDuration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void blinkDuration(MillisRange blinkDuration) {
        throw new UnsupportedOperationException();
    }

}
