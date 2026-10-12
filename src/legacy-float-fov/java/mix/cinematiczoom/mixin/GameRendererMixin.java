package mix.cinematiczoom.mixin;

import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$removeBobView(CallbackInfo ci) {
        if (ZoomManager.isZoomActive() && ZoomManager.shouldRemoveBobbing()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideHands(CallbackInfo ci) {
        if (ZoomManager.shouldHideHands()) {
            ci.cancel();
        }
    }

    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)F",
            at = @At("RETURN"), cancellable = true)
    private void cinematiczoom$applyZoom(Camera camera, float tickDelta, boolean changingFov,
                                         CallbackInfoReturnable<Float> cir) {
        ZoomManager.frameUpdate();

        float fov = cir.getReturnValue();
        ZoomManager.setBaseFov(fov);
        double mul = ZoomManager.getCurrentFovMul();
        if (mul != 1.0) {
            float zoomed = (float) (fov * mul);
            if (zoomed > 175.0f) zoomed = 175.0f;
            cir.setReturnValue(zoomed);
        }
    }
}
