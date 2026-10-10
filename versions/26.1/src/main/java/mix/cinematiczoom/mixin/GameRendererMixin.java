package mix.cinematiczoom.mixin;

import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$removeBobView(CallbackInfo ci) {
        if (ZoomManager.isZoomActive() && ZoomManager.shouldRemoveBobbing()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideHands(CallbackInfo ci) {
        if (ZoomManager.shouldHideHands()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "extractGui(Lnet/minecraft/client/DeltaTracker;ZZ)V", at = @At("STORE"), ordinal = 0)
    private GuiGraphicsExtractor cinematiczoom$drawBarsFirst(GuiGraphicsExtractor ctx) {
        ZoomManager.renderBars(ctx);
        return ctx;
    }
}
