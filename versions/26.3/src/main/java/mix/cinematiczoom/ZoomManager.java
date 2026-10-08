package mix.cinematiczoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ZoomManager {
    private static final ZoomController ZOOM = new ZoomController();

    private static boolean hudForcedByUs;
    private static boolean smoothCameraForcedByUs;
    private static boolean cinematicToggled;
    private static boolean regularToggled;
    private static boolean wasCinematicDown;
    private static boolean wasRegularDown;
    private static ZoomMode currentAppliedMode = ZoomMode.NONE;

    private ZoomManager() {
    }

    public static void tick(Minecraft client, KeyMapping key) {
        tick(client, key, null);
    }

    public static void tick(Minecraft client, KeyMapping keyWithBars, KeyMapping keyNoBars) {
        boolean inWorld = client.level != null && client.player != null;
        boolean canInteract = inWorld
                && client.gui.screen() == null
                && client.isWindowActive();

        boolean isCinematicDown = canInteract && isZoomKeyDown(client, keyWithBars);
        boolean isRegularDown = canInteract && isZoomKeyDown(client, keyNoBars);

        if (!inWorld) {
            cinematicToggled = false;
            regularToggled = false;
        }

        ZoomConfig cfg = ZoomConfig.INSTANCE;

        if (cfg.cinematicToggle && isCinematicDown && !wasCinematicDown) {
            cinematicToggled = !cinematicToggled;
            if (cinematicToggled) {
                regularToggled = false;
            }
        }
        if (cfg.regularToggle && isRegularDown && !wasRegularDown) {
            regularToggled = !regularToggled;
            if (regularToggled) {
                cinematicToggled = false;
            }
        }

        ZoomMode desiredMode = ZoomMode.NONE;
        if (cinematicToggled && inWorld) {
            desiredMode = ZoomMode.CINEMATIC;
        } else if (regularToggled && inWorld) {
            desiredMode = ZoomMode.REGULAR;
        } else if (!cfg.cinematicToggle && isCinematicDown) {
            desiredMode = canInteract ? ZoomMode.CINEMATIC : ZoomMode.NONE;
        } else if (!cfg.regularToggle && isRegularDown) {
            desiredMode = canInteract ? ZoomMode.REGULAR : ZoomMode.NONE;
        }

        wasCinematicDown = isCinematicDown;
        wasRegularDown = isRegularDown;

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
        cinematicToggled = false;
        regularToggled = false;
        wasCinematicDown = false;
        wasRegularDown = false;
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
        if (!ZOOM.isActive()) {
            return 1.0;
        }
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        ZoomMode mode = ZOOM.getActiveMode();
        boolean shouldScale = (mode == ZoomMode.REGULAR)
                ? cfg.regularScaleSensitivity
                : cfg.cinematicScaleSensitivity;
        return shouldScale ? ZOOM.currentMultiplier() : 1.0;
    }

    public static boolean shouldRemoveBobbing() {
        if (!ZOOM.isActive()) {
            return false;
        }
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        ZoomMode mode = ZOOM.getActiveMode();
        return (mode == ZoomMode.REGULAR)
                ? cfg.regularRemoveBobbing
                : cfg.cinematicRemoveBobbing;
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
