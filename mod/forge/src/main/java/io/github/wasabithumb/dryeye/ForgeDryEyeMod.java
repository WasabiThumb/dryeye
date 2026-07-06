package io.github.wasabithumb.dryeye;

import com.mojang.logging.LogUtils;
import io.github.wasabithumb.dryeye.config.screen.DryEyeConfigScreen;
import io.github.wasabithumb.dryeye.manager.ForgeDryEyeManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

@Mod(ForgeDryEyeMod.MOD_ID)
@NullMarked
public final class ForgeDryEyeMod extends DryEye {

    public static final String MOD_ID = "dryeye";
    public static final Logger LOGGER = LogUtils.getLogger();

    //

    public ForgeDryEyeMod(FMLJavaModLoadingContext ctx) {
        BusGroup busGroup = ctx.getModBusGroup();
        FMLClientSetupEvent.getBus(busGroup).addListener(this::setup);
        ctx.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(DryEyeConfigScreen::new)
        );
    }

    //

    @Override
    protected ForgeDryEyeManager newManager() {
        return ForgeDryEyeManager.create(Minecraft.getInstance());
    }

    private void setup(FMLClientSetupEvent event) {
        this.start();
    }

}
