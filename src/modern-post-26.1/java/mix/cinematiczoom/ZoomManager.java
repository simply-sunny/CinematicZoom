package mix.cinematiczoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ZoomManager {
    private static final ZoomController ZOOM = new ZoomController();

    private static boolean hudForcedByUs;
    private static boolean smoothCameraForcedByUs;
    private static final ZoomInputState INPUT = new ZoomInputState();
    private static ZoomMode currentAppliedMode = ZoomMode.NONE;

    private ZoomManager() {
    }

    public static void tick(Minecraft client, KeyMapping cinematicKey, KeyMapping regularKey) {
        boolean inWorld = client.level != null && client.player != null;
        boolean canInteract = inWorld
                && client.gui.screen() == null
                && client.isWindowActive();

        boolean isCinematicDown = canInteract && isZoomKeyDown(client, cinematicKey);
        boolean isRegularDown = canInteract && isZoomKeyDown(client, regularKey);

        ZoomMode desiredMode = INPUT.update(inWorld, isCinematicDown, isRegularDown);

        ZOOM.update(desiredMode);

        if (desiredMode != currentAppliedMode) {
            applyOverrides(client, desiredMode);
            currentAppliedMode = desiredMode;
        }
    }

    private static void applyOverrides(Minecraft client, ZoomMode mode) {
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        boolean wantHideHud = (mode == ZoomMode.CINEMATIC && cfg.cinematicHideHud)
                || (mode == ZoomMode.REGULAR && cfg.regularHideHud);
        boolean wantSmoothCam = (mode == ZoomMode.CINEMATIC && cfg.cinematicCamera)
                || (mode == ZoomMode.REGULAR && cfg.regularCinematicCamera);

        if (wantHideHud) {
            if (!client.gui.hud.isHidden()) {
                client.gui.hud.toggle();
                hudForcedByUs = true;
            }
        } else if (hudForcedByUs) {
            if (client.gui.hud.isHidden()) {
                client.gui.hud.toggle();
            }
            hudForcedByUs = false;
        }

        if (wantSmoothCam) {
            if (!client.options.smoothCamera) {
                client.options.smoothCamera = true;
                smoothCameraForcedByUs = true;
            }
        } else if (smoothCameraForcedByUs) {
            client.options.smoothCamera = false;
            smoothCameraForcedByUs = false;
        }
    }

    private static boolean isZoomKeyDown(Minecraft client, KeyMapping key) {
        return key != null && !key.isUnbound() && key.isDown();
    }

    public static void reset(Minecraft client) {
        INPUT.reset();
        applyOverrides(client, ZoomMode.NONE);
        currentAppliedMode = ZoomMode.NONE;
        ZOOM.reset();
    }

    public static void frameUpdate() {
        ZOOM.updateFrame();
    }

    public static double getCurrentFovMul() {
        return ZOOM.currentMultiplier();
    }

    public static boolean isZoomActive() {
        return ZOOM.isActive();
    }

    public static double getSensitivityMultiplier() {
        return ZOOM.getSensitivityMultiplier();
    }

    public static boolean shouldRemoveBobbing() {
        return ZOOM.shouldRemoveBobbing();
    }

    public static boolean onWheel(double vertical) {
        return ZOOM.onWheel(vertical);
    }

    public static void renderBars(GuiGraphicsExtractor context) {
        float barsPercent = ZOOM.currentBarsPercent();
        if (barsPercent <= 0.0001f) return;

        int width = context.guiWidth();
        int height = context.guiHeight();
        int barHeight = Math.round(height * barsPercent / 100f);
        if (barHeight <= 0) return;

        context.fill(0, 0, width, barHeight, 0xFF000000);
        context.fill(0, height - barHeight, width, height, 0xFF000000);
    }
}
