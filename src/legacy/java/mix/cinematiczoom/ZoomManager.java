package mix.cinematiczoom;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ZoomManager {
    private static final ZoomController ZOOM = new ZoomController();

    private static boolean smoothCameraForcedByUs;
    private static final ZoomInputState INPUT = new ZoomInputState();
    private static ZoomMode currentAppliedMode = ZoomMode.NONE;

    private ZoomManager() {
    }

    public static void tick(MinecraftClient client, KeyBinding cinematicKey, KeyBinding regularKey) {
        boolean inWorld = client.world != null && client.player != null;
        boolean canInteract = inWorld
                && client.currentScreen == null
                && client.isWindowFocused();

        boolean isCinematicDown = canInteract && isZoomKeyPressed(client, cinematicKey);
        boolean isRegularDown = canInteract && isZoomKeyPressed(client, regularKey);

        ZoomMode desiredMode = INPUT.update(inWorld, isCinematicDown, isRegularDown);

        ZOOM.update(desiredMode);

        if (desiredMode != currentAppliedMode) {
            applyOverrides(client, desiredMode);
            currentAppliedMode = desiredMode;
        }
    }

    private static void applyOverrides(MinecraftClient client, ZoomMode mode) {
        ZoomConfig cfg = ZoomConfig.INSTANCE;
        boolean wantHideHud = (mode == ZoomMode.CINEMATIC && cfg.cinematicHideHud)
                || (mode == ZoomMode.REGULAR && cfg.regularHideHud);
        boolean wantSmoothCam = (mode == ZoomMode.CINEMATIC && cfg.cinematicCamera)
                || (mode == ZoomMode.REGULAR && cfg.regularCinematicCamera);

        HudController.setHidden(client, wantHideHud);

        if (wantSmoothCam) {
            if (!client.options.smoothCameraEnabled) {
                client.options.smoothCameraEnabled = true;
                smoothCameraForcedByUs = true;
            }
        } else if (smoothCameraForcedByUs) {
            client.options.smoothCameraEnabled = false;
            smoothCameraForcedByUs = false;
        }
    }

    private static boolean isZoomKeyPressed(MinecraftClient client, KeyBinding key) {
        if (key == null) {
            return false;
        }
        if (key.isPressed()) {
            return true;
        }
        if (key.isUnbound() || client.getWindow() == null) {
            return false;
        }
        InputUtil.Key boundKey = KeyBindingHelper.getBoundKeyOf(key);
        if (boundKey.getCode() == InputUtil.UNKNOWN_KEY.getCode()) {
            return false;
        }
        long handle = client.getWindow().getHandle();
        if (boundKey.getCategory() == InputUtil.Type.KEYSYM) {
            return GLFW.glfwGetKey(handle, boundKey.getCode()) == GLFW.GLFW_PRESS;
        }
        if (boundKey.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(handle, boundKey.getCode()) == GLFW.GLFW_PRESS;
        }
        return false;
    }

    public static void reset(MinecraftClient client) {
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

    public static boolean shouldHideHud() {
        return HudController.shouldHideHud();
    }

    public static void renderBars(DrawContext context) {
        float barsPercent = ZOOM.currentBarsPercent();
        if (barsPercent <= 0.0001f) return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int barHeight = Math.round(height * barsPercent / 100f);
        if (barHeight <= 0) return;

        context.fill(0, 0, width, barHeight, 0xFF000000);
        context.fill(0, height - barHeight, width, height, 0xFF000000);
    }
}
