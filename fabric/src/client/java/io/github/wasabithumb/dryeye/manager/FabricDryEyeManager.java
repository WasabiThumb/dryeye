package io.github.wasabithumb.dryeye.manager;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NullMarked;
import org.slf4j.LoggerFactory;

@NullMarked
public final class FabricDryEyeManager extends AbstractDryEyeManager {

    public FabricDryEyeManager() {
        super(
                Minecraft.getInstance(),
                LoggerFactory.getLogger("DryEye"),
                FabricLoader.getInstance().getConfigDir().resolve("dryeye.toml")
        );
    }

}
