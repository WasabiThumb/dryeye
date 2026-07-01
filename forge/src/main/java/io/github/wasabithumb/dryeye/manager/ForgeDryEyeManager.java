package io.github.wasabithumb.dryeye.manager;

import io.github.wasabithumb.dryeye.ForgeDryEyeMod;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

import java.nio.file.Path;

@NullMarked
public final class ForgeDryEyeManager extends AbstractDryEyeManager {

    public static ForgeDryEyeManager create(Minecraft mc) {
        return new ForgeDryEyeManager(
                mc,
                ForgeDryEyeMod.LOGGER,
                mc.gameDirectory.toPath().resolve("config/dryeye.toml")
        );
    }

    //

    private ForgeDryEyeManager(
            Minecraft mc,
            Logger logger,
            Path configPath
    ) {
        super(mc, logger, configPath);
    }

}
