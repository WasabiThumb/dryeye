package io.github.wasabithumb.dryeye;

import io.github.wasabithumb.dryeye.manager.DryEyeManager;
import net.fabricmc.api.ClientModInitializer;

public final class DryEyeMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DryEyeManager.getInstance().loadConfig();
    }

}
