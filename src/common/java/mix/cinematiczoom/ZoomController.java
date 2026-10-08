package mix.cinematiczoom;

final class ZoomController {
    private ZoomMode activeMode = ZoomMode.NONE;
    private ZoomMode lastActiveMode = ZoomMode.CINEMATIC;

    private float currentMultiplier = 1.0f;
    private float targetMultiplier = 1.0f;
    private float heldMultiplier = startingMultiplier(ZoomMode.CINEMATIC);
    private float currentBarsPercent;
    private float targetBarsPercent;

    private float animStartMultiplier = 1.0f;
    private float animStartBarsPercent = 0.0f;
    private double animElapsedMs = 0.0;
    private int animDurationMs = 240;
    private ZoomCurve animCurve = ZoomCurve.EXPONENTIAL;

    private long lastFrameNanos;

    boolean update(boolean shouldZoom) {
        return update(shouldZoom ? ZoomMode.CINEMATIC : ZoomMode.NONE);
    }

    boolean update(boolean shouldZoom, boolean showBars) {
        if (!shouldZoom) {
            return update(ZoomMode.NONE);
        }
        return update(showBars ? ZoomMode.CINEMATIC : ZoomMode.REGULAR);
    }

    boolean update(ZoomMode mode) {
        boolean starting = mode.isActive() && !activeMode.isActive();
        boolean modeChanged = mode != activeMode;

        if (mode.isActive()) {
            lastActiveMode = mode;
        }

        if (starting || (mode.isActive() && modeChanged)) {
            heldMultiplier = startingMultiplier(mode);
        }

        this.activeMode = mode;

        float prevTargetMultiplier = targetMultiplier;
        float prevTargetBars = targetBarsPercent;

        if (mode == ZoomMode.CINEMATIC) {
            targetMultiplier = heldMultiplier;
            targetBarsPercent = ZoomConfig.INSTANCE.cinematicBarsPercent;
        } else if (mode == ZoomMode.REGULAR) {
            targetMultiplier = heldMultiplier;
            targetBarsPercent = 0f;
        } else {
            targetMultiplier = 1.0f;
            targetBarsPercent = 0f;
        }

        if (targetMultiplier != prevTargetMultiplier || targetBarsPercent != prevTargetBars) {
            startAnimation(mode.isActive());
        }

        return starting;
    }

    void reset() {
        activeMode = ZoomMode.NONE;
        lastActiveMode = ZoomMode.CINEMATIC;
        currentMultiplier = 1.0f;
        targetMultiplier = 1.0f;
        heldMultiplier = startingMultiplier(ZoomMode.CINEMATIC);
        currentBarsPercent = 0f;
        targetBarsPercent = 0f;
        animStartMultiplier = 1.0f;
        animStartBarsPercent = 0f;
        animElapsedMs = 0.0;
        animDurationMs = ZoomConfig.INSTANCE.cinematicZoomOutMs;
        animCurve = ZoomConfig.INSTANCE.cinematicZoomOutCurve;
        lastFrameNanos = 0L;
    }

    void updateFrame() {
        long now = System.nanoTime();
        if (lastFrameNanos == 0L) {
            lastFrameNanos = now;
            return;
        }

        double deltaMs = Math.min((now - lastFrameNanos) * 1e-6, 50.0);
        lastFrameNanos = now;

        if (animDurationMs <= 0) {
            currentMultiplier = targetMultiplier;
            currentBarsPercent = targetBarsPercent;
            return;
        }

        if (currentMultiplier == targetMultiplier && currentBarsPercent == targetBarsPercent) {
            return;
        }

        animElapsedMs += deltaMs;
        double progress = animDurationMs > 0 ? Math.min(1.0, animElapsedMs / animDurationMs) : 1.0;
        double eased = animCurve != null ? animCurve.apply(progress) : progress;

        currentMultiplier = (float) lerp(animStartMultiplier, targetMultiplier, eased);
        currentBarsPercent = (float) lerp(animStartBarsPercent, targetBarsPercent, eased);

        if (progress >= 1.0 || Math.abs(currentMultiplier - targetMultiplier) < 1e-4f) {
            currentMultiplier = targetMultiplier;
        }
        if (progress >= 1.0 || Math.abs(currentBarsPercent - targetBarsPercent) < 1e-3f) {
            currentBarsPercent = targetBarsPercent;
        }
    }

    private void startAnimation(boolean zoomingIn) {
        animStartMultiplier = currentMultiplier;
        animStartBarsPercent = currentBarsPercent;
        animElapsedMs = 0.0;

        ZoomConfig cfg = ZoomConfig.INSTANCE;
        ZoomMode profileMode = activeMode.isActive() ? activeMode : lastActiveMode;

        if (profileMode == ZoomMode.REGULAR) {
            animDurationMs = zoomingIn ? cfg.regularZoomInMs : cfg.regularZoomOutMs;
            animCurve = zoomingIn ? cfg.regularZoomInCurve : cfg.regularZoomOutCurve;
        } else {
            animDurationMs = zoomingIn ? cfg.cinematicZoomInMs : cfg.cinematicZoomOutMs;
            animCurve = zoomingIn ? cfg.cinematicZoomInCurve : cfg.cinematicZoomOutCurve;
        }

        if (animCurve == null) {
            animCurve = ZoomCurve.EXPONENTIAL;
        }
    }

    boolean onWheel(double vertical) {
        if (!activeMode.isActive() || !ZoomConfig.INSTANCE.mouseWheelEnabled || vertical == 0.0) {
            return false;
        }

        float prevTarget = heldMultiplier;
        ZoomConfig cfg = ZoomConfig.INSTANCE;

        if (cfg.discreteScroll) {
            float currentZoom = heldMultiplier <= 0f ? 1.0f : 1.0f / heldMultiplier;
            float step = Math.max(0.1f, cfg.discreteScrollStep);
            float newZoom;
            if (vertical > 0) {
                int notches = (int) Math.round(vertical);
                if (notches < 1) notches = 1;
                newZoom = (float) (Math.floor(currentZoom / step + 1e-4) + notches) * step;
            } else {
                int notches = (int) Math.round(-vertical);
                if (notches < 1) notches = 1;
                newZoom = (float) (Math.ceil(currentZoom / step - 1e-4) - notches) * step;
            }
            float minZoom = 1.0f / cfg.maxZoomMultiplier;
            float maxZoom = 1.0f / cfg.minZoomMultiplier;
            newZoom = clamp(newZoom, minZoom, maxZoom);
            heldMultiplier = 1.0f / newZoom;
        } else {
            heldMultiplier = clamp(
                    heldMultiplier - (float) vertical * cfg.wheelStep,
                    cfg.minZoomMultiplier,
                    cfg.maxZoomMultiplier
            );
        }
        targetMultiplier = heldMultiplier;

        if (targetMultiplier != prevTarget) {
            startAnimation(true);
        }
        return true;
    }

    double currentMultiplier() {
        return currentMultiplier;
    }

    ZoomMode getActiveMode() {
        return activeMode;
    }

    boolean isActive() {
        return activeMode.isActive() || Math.abs(currentMultiplier - 1.0f) > 1e-4f || currentBarsPercent > 1e-3f;
    }

    float currentBarsPercent() {
        return currentBarsPercent;
    }

    private static double lerp(double start, double end, double amount) {
        return start + (end - start) * amount;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float startingMultiplier(ZoomMode mode) {
        float zoom = (mode == ZoomMode.REGULAR)
                ? ZoomConfig.INSTANCE.regularStartingZoom
                : ZoomConfig.INSTANCE.cinematicStartingZoom;
        return clamp(
                zoom <= 0f ? 1.0f : 1.0f / zoom,
                ZoomConfig.INSTANCE.minZoomMultiplier,
                ZoomConfig.INSTANCE.maxZoomMultiplier
        );
    }
}
