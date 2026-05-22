package io.github.wasabithumb.dryeye.mixin;

import io.github.wasabithumb.dryeye.manager.DryEyeManager;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
@NullMarked
public class MinecraftMixin {

    @Inject(at = @At("TAIL"), method = "runTick")
    public void runTick(boolean advanceGameTime, CallbackInfo ci) {
        DryEyeManager.getInstance().cleanup();
    }

}
