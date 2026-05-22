package io.github.wasabithumb.dryeye.mixin;

import io.github.wasabithumb.dryeye.manager.DryEyeManager;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@NullMarked
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    @Inject(at = @At("HEAD"), method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;", cancellable = true)
    public void getTextureLocation(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
        DryEyeManager manager = DryEyeManager.getInstance();
        DryEyeManager.BlinkState blinkState = manager.query(state.id, state.skin.body().texturePath());
        if (blinkState.active()) cir.setReturnValue(blinkState.modifiedSkin());
    }

}
