package io.github.wasabithumb.dryeye;

import com.mojang.logging.LogUtils;
import io.github.wasabithumb.dryeye.config.screen.DryEyeConfigScreenFactory;
import io.github.wasabithumb.dryeye.manager.NeoForgeDryEyeManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

@NullMarked
@Mod(value = NeoForgeDryEyeMod.MODID, dist = Dist.CLIENT)
public final class NeoForgeDryEyeMod extends DryEye {

    public static final String MODID = "dryeye";
    public static final Logger LOGGER = LogUtils.getLogger();

    //

    public NeoForgeDryEyeMod(ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
        container.registerExtensionPoint(IConfigScreenFactory.class, new DryEyeConfigScreenFactory());
    }

    //

    @Override
    protected NeoForgeDryEyeManager newManager() {
        return NeoForgeDryEyeManager.create(Minecraft.getInstance());
    }

    @SubscribeEvent
    public void onClientStarted(ClientStartedEvent event) {
        this.start();
    }

    @SubscribeEvent
    public void onClientStopping(ClientStoppingEvent event) {
        this.stop();
    }

}
