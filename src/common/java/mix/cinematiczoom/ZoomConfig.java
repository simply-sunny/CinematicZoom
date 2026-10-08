package mix.cinematiczoom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ZoomConfig {
    public static final ZoomConfig INSTANCE = new ZoomConfig();

    private static final Logger LOGGER = LoggerFactory.getLogger("cinematiczoom");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("cinematiczoom.json");

    // Cinematic Zoom Profile
    public float cinematicStartingZoom = 3.0f;
    public float cinematicBarsPercent = 15.0f;
    public int cinematicZoomInMs = 240;
    public int cinematicZoomOutMs = 240;
    public ZoomCurve cinematicZoomInCurve = ZoomCurve.EXPONENTIAL;
    public ZoomCurve cinematicZoomOutCurve = ZoomCurve.EXPONENTIAL;
    public boolean cinematicHideHud = true;
    public boolean cinematicCamera = true;
    public boolean cinematicScaleSensitivity = true;
    public boolean cinematicToggle = false;

    // Regular Zoom Profile (No Bars)
    public float regularStartingZoom = 3.0f;
    public int regularZoomInMs = 240;
    public int regularZoomOutMs = 240;
    public ZoomCurve regularZoomInCurve = ZoomCurve.EXPONENTIAL;
    public ZoomCurve regularZoomOutCurve = ZoomCurve.EXPONENTIAL;
    public boolean regularHideHud = false;
    public boolean regularCinematicCamera = false;
    public boolean regularScaleSensitivity = true;
    public boolean regularToggle = false;

    // Shared / Wheel Settings
    public boolean mouseWheelEnabled = true;
    public boolean discreteScroll = false;
    public float discreteScrollStep = 1.0f;
    public float minZoomMultiplier = 1.0f / 30.0f;
    public float maxZoomMultiplier = 1.00f;
    public float wheelStep = 0.05f;

    public void load() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            ZoomConfig loaded = GSON.fromJson(json, ZoomConfig.class);
            if (loaded != null) {
                // Migration for legacy configs
                if (!json.has("cinematicStartingZoom")) {
                    if (json.has("startingZoom")) {
                        loaded.cinematicStartingZoom = json.get("startingZoom").getAsFloat();
                    } else if (json.has("baseZoomMultiplier")) {
                        float oldMultiplier = json.get("baseZoomMultiplier").getAsFloat();
                        if (oldMultiplier <= 0f) {
                            loaded.cinematicStartingZoom = 1.0f;
                        } else if (Math.abs(oldMultiplier - 0.33f) < 0.01f) {
                            loaded.cinematicStartingZoom = 3.0f;
                        } else {
                            loaded.cinematicStartingZoom = Math.round(10.0f / oldMultiplier) / 10.0f;
                        }
                    }
                }
                if (!json.has("regularStartingZoom") && json.has("startingZoom")) {
                    loaded.regularStartingZoom = json.get("startingZoom").getAsFloat();
                }
                if (!json.has("cinematicBarsPercent") && json.has("barsPercent")) {
                    loaded.cinematicBarsPercent = json.get("barsPercent").getAsFloat();
                }
                if (json.has("smoothMs")) {
                    int oldSmooth = json.get("smoothMs").getAsInt();
                    if (!json.has("cinematicZoomInMs")) loaded.cinematicZoomInMs = oldSmooth;
                    if (!json.has("cinematicZoomOutMs")) loaded.cinematicZoomOutMs = oldSmooth;
                    if (!json.has("regularZoomInMs")) loaded.regularZoomInMs = oldSmooth;
                    if (!json.has("regularZoomOutMs")) loaded.regularZoomOutMs = oldSmooth;
                }
                if (json.has("zoomInMs") && !json.has("cinematicZoomInMs")) {
                    loaded.cinematicZoomInMs = json.get("zoomInMs").getAsInt();
                }
                if (json.has("zoomOutMs") && !json.has("cinematicZoomOutMs")) {
                    loaded.cinematicZoomOutMs = json.get("zoomOutMs").getAsInt();
                }
                if (json.has("hideHudDuringZoom") && !json.has("cinematicHideHud")) {
                    loaded.cinematicHideHud = json.get("hideHudDuringZoom").getAsBoolean();
                }
                if (json.has("enableCinematicCamera") && !json.has("cinematicCamera")) {
                    loaded.cinematicCamera = json.get("enableCinematicCamera").getAsBoolean();
                }
                if (json.has("toggleMode") && !json.has("cinematicToggle")) {
                    loaded.cinematicToggle = json.get("toggleMode").getAsBoolean();
                }

                copyFrom(loaded);
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load config from {}", PATH, exception);
        }
        clamp();
    }

    public void save() {
        clamp();
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            LOGGER.warn("Failed to save config to {}", PATH, exception);
        }
    }

    public void clamp() {
        cinematicBarsPercent = Math.max(0f, Math.min(50f, cinematicBarsPercent));

        cinematicZoomInMs = Math.max(0, cinematicZoomInMs);
        cinematicZoomOutMs = Math.max(0, cinematicZoomOutMs);
        if (cinematicZoomInCurve == null) cinematicZoomInCurve = ZoomCurve.EXPONENTIAL;
        if (cinematicZoomOutCurve == null) cinematicZoomOutCurve = ZoomCurve.EXPONENTIAL;

        regularZoomInMs = Math.max(0, regularZoomInMs);
        regularZoomOutMs = Math.max(0, regularZoomOutMs);
        if (regularZoomInCurve == null) regularZoomInCurve = ZoomCurve.EXPONENTIAL;
        if (regularZoomOutCurve == null) regularZoomOutCurve = ZoomCurve.EXPONENTIAL;

        maxZoomMultiplier = Math.max(0.01f, Math.min(1.0f, maxZoomMultiplier));
        minZoomMultiplier = Math.max(0.01f, Math.min(maxZoomMultiplier, minZoomMultiplier));

        float minStarting = 1.0f / maxZoomMultiplier;
        float maxStarting = 1.0f / minZoomMultiplier;

        cinematicStartingZoom = Math.max(minStarting, Math.min(maxStarting, cinematicStartingZoom));
        regularStartingZoom = Math.max(minStarting, Math.min(maxStarting, regularStartingZoom));

        discreteScrollStep = Math.max(0.1f, Math.min(5.0f, discreteScrollStep));
        wheelStep = Math.max(0.01f, Math.min(0.25f, wheelStep));
    }

    private void copyFrom(ZoomConfig other) {
        cinematicStartingZoom = other.cinematicStartingZoom;
        cinematicBarsPercent = other.cinematicBarsPercent;
        cinematicZoomInMs = other.cinematicZoomInMs;
        cinematicZoomOutMs = other.cinematicZoomOutMs;
        cinematicZoomInCurve = other.cinematicZoomInCurve;
        cinematicZoomOutCurve = other.cinematicZoomOutCurve;
        cinematicHideHud = other.cinematicHideHud;
        cinematicCamera = other.cinematicCamera;
        cinematicScaleSensitivity = other.cinematicScaleSensitivity;
        cinematicToggle = other.cinematicToggle;

        regularStartingZoom = other.regularStartingZoom;
        regularZoomInMs = other.regularZoomInMs;
        regularZoomOutMs = other.regularZoomOutMs;
        regularZoomInCurve = other.regularZoomInCurve;
        regularZoomOutCurve = other.regularZoomOutCurve;
        regularHideHud = other.regularHideHud;
        regularCinematicCamera = other.regularCinematicCamera;
        regularScaleSensitivity = other.regularScaleSensitivity;
        regularToggle = other.regularToggle;

        mouseWheelEnabled = other.mouseWheelEnabled;
        discreteScroll = other.discreteScroll;
        discreteScrollStep = other.discreteScrollStep;
        minZoomMultiplier = other.minZoomMultiplier;
        maxZoomMultiplier = other.maxZoomMultiplier;
        wheelStep = other.wheelStep;
    }
}
