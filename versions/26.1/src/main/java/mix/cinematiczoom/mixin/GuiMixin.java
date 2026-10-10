package mix.cinematiczoom.mixin;

import mix.cinematiczoom.ZoomManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideCrosshair(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        if (ZoomManager.shouldHideCrosshair()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void cinematiczoom$hideHotbar(GuiGraphicsExtractor extractor, DeltaTracker tracker, CallbackInfo ci) {
        if (ZoomManager.shouldHideHud()) {
            ci.cancel();
        }
    }
}
