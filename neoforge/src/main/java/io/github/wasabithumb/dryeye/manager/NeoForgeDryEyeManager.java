package io.github.wasabithumb.dryeye.manager;

import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

@NullMarked
public final class NeoForgeDryEyeManager extends AbstractDryEyeManager {

    public static NeoForgeDryEyeManager create(Minecraft mc) {
        return new NeoForgeDryEyeManager(
                mc,
                LoggerFactory.getLogger("DryEye"),
                mc.gameDirectory.toPath().resolve("config/dryeye.toml")
        );
    }

    //

    private NeoForgeDryEyeManager(
            Minecraft mc,
            Logger logger,
            Path configPath
    ) {
        super(mc, logger, configPath);
    }

}
