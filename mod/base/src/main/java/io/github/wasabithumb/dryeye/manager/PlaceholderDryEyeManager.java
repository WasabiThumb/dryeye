package io.github.wasabithumb.dryeye.manager;

import io.github.wasabithumb.dryeye.config.DryEyeConfig;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

@NullMarked
@ApiStatus.Internal
final class PlaceholderDryEyeManager implements DryEyeManager {

    PlaceholderDryEyeManager() { }

    //

    @Override
    public Logger logger() {
        return NOPLogger.NOP_LOGGER;
    }

    @Override
    public DryEyeConfig config() {
        return DryEyeConfig.disabled();
    }

    @Override
    public void loadConfig() {
        // NOP
    }

    @Override
    public void saveConfig() {
        // NOP
    }

    @Override
    public BlinkState query(int id, Identifier skin) {
        return InactiveBlinkState.INSTANCE;
    }

    @Override
    public void cleanup() {
        // NOP
    }

}
