package io.github.wasabithumb.dryeye;

import io.github.wasabithumb.dryeye.manager.FabricDryEyeManager;
import net.fabricmc.api.ClientModInitializer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FabricDryEyeMod extends DryEye implements ClientModInitializer {

    @Override
    protected FabricDryEyeManager newManager() {
        return new FabricDryEyeManager();
    }

    @Override
    public void onInitializeClient() {
        this.start();
    }

}
