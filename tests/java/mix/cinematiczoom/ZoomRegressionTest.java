package mix.cinematiczoom;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ZoomRegressionTest {
    private static final ZoomConfig CFG = ZoomConfig.INSTANCE;
    private static Minecraft client;
    private static KeyMapping cinematic;
    private static KeyMapping regular;
    private static KeyMapping barsUp;
    private static KeyMapping barsDown;
    private static int failures;
    private static int checks;

    public static void main(String[] args) throws Exception {
        run("disabling cinematic toggle releases zoom", () -> disabledToggle(false));
        run("disabling regular toggle releases zoom", () -> disabledToggle(true));
        run("regular sensitivity setting survives release", () -> exitProfile(false));
        run("regular sensitivity scaling survives release", () -> exitProfile(true));
        run("legacy default limit upgrades to 30x", () -> {
            load("{\"startingZoom\":3,\"minZoomMultiplier\":0.1}");
            CFG.cinematicStartingZoom = 30;
            CFG.save();
            near(CFG.cinematicStartingZoom, 30);
            CFG.load();
            near(CFG.cinematicStartingZoom, 30);
        });
        run("custom legacy limit stays at 5x", () -> {
            load("{\"startingZoom\":3,\"minZoomMultiplier\":0.2}");
            CFG.cinematicStartingZoom = 30;
            CFG.save();
            near(CFG.cinematicStartingZoom, 5);
        });
        run("new-profile custom limit is not migrated", () -> {
            load("{\"cinematicStartingZoom\":3,\"minZoomMultiplier\":0.1}");
            CFG.cinematicStartingZoom = 30;
            CFG.save();
            near(CFG.cinematicStartingZoom, 10);
        });
        run("legacy profile settings are preserved", () -> {
            load("{\"startingZoom\":4,\"smoothMs\":100,\"barsPercent\":20,"
                    + "\"hideHudDuringZoom\":false,\"enableCinematicCamera\":false,\"toggleMode\":true}");
            near(CFG.cinematicStartingZoom, 4);
            near(CFG.regularStartingZoom, 4);
            near(CFG.cinematicBarsPercent, 20);
            near(CFG.cinematicZoomInMs, 100);
            near(CFG.regularZoomOutMs, 100);
            require(!CFG.cinematicHideHud && !CFG.cinematicCamera && CFG.cinematicToggle,
                    "legacy flags were lost");
        });
        run("toggle profiles switch and disconnect resets state", () -> {
            CFG.cinematicToggle = true;
            CFG.regularToggle = true;
            CFG.regularHideHud = false;
            cinematic.down = true;
            tick();
            cinematic.down = false;
            regular.down = true;
            tick();
            require(!ZoomManager.shouldHideHud(), "regular mode should restore HUD");
            require(!client.options.smoothCamera, "regular mode should restore camera");
            client.level = null;
            tick();
            client.level = new Object();
            regular.down = false;
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "disconnect left a toggle active");
        });
        run("switching profiles preserves the held zoom level", () -> {
            CFG.regularStartingZoom = 6;
            CFG.cinematicStartingZoom = 2;
            regular.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 6);
            regular.down = false;
            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 6);
            require(ZoomManager.shouldHideHud(), "cinematic profile did not hide HUD");
        });
        run("hold zoom stops when a screen opens", () -> {
            cinematic.down = true;
            tick();
            client.gui.screen = client.screen = new Object();
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "hold zoom stayed active behind a screen");
        });
        run("discrete wheel steps forward and back", () -> {
            CFG.discreteScroll = true;
            regular.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
            require(ZoomManager.onWheel(1), "active wheel event was not consumed");
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.25);
            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
        });
        run("discrete wheel snaps off-grid zoom and respects limits", () -> {
            CFG.discreteScroll = true;
            CFG.regularStartingZoom = 3.4f;
            regular.down = true;
            tick();
            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.25);
            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
            ZoomManager.onWheel(100);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 30);
            ZoomManager.onWheel(-100);
            settle();
            near(ZoomManager.getCurrentFovMul(), CFG.maxZoomMultiplier);
        });
        run("zero and disabled wheel events are not consumed", () -> {
            regular.down = true;
            tick();
            require(!ZoomManager.onWheel(0), "zero wheel event was consumed");
            CFG.mouseWheelEnabled = false;
            require(!ZoomManager.onWheel(1), "disabled wheel event was consumed");
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
        });
        run("wheel respects both zoom boundaries", () -> {
            regular.down = true;
            tick();
            ZoomManager.onWheel(100);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 30);
            ZoomManager.onWheel(-100);
            settle();
            near(ZoomManager.getCurrentFovMul(), CFG.maxZoomMultiplier);
            regular.down = false;
            tick();
            require(!ZoomManager.onWheel(1), "inactive wheel event was consumed");
        });
        run("wheel allows ultrawide backward zoom past 1x", () -> {
            CFG.allowUltrawide = true;
            CFG.clamp();
            regular.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 3);
            ZoomManager.onWheel(-100);
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);
            require(ZoomManager.isZoomActive(), "ultrawide zoom should remain active");
            near(ZoomManager.getSensitivityMultiplier(), 1.0);
            CFG.allowUltrawide = false;
            CFG.clamp();
        });
        run("wheel does not go past 1x when ultrawide is disabled", () -> {
            CFG.allowUltrawide = false;
            CFG.clamp();
            regular.down = true;
            tick();
            settle();
            ZoomManager.onWheel(-100);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0);
        });
        run("hands and hud reappear instantly on release without waiting for animation", () -> {
            CFG.cinematicHideHud = true;
            CFG.cinematicHideHands = true;
            CFG.cinematicHideCrosshair = true;
            CFG.cinematicZoomOutMs = 500;
            cinematic.down = true;
            tick();
            require(ZoomManager.shouldHideHud(), "hud should be hidden while holding");
            require(ZoomManager.shouldHideHands(), "hands should be hidden while holding");
            require(ZoomManager.shouldHideCrosshair(), "crosshair should be hidden while holding");
            cinematic.down = false;
            tick();
            require(!ZoomManager.shouldHideHud(), "hud should reappear instantly on release");
            require(!ZoomManager.shouldHideHands(), "hands should reappear instantly on release");
            require(!ZoomManager.shouldHideCrosshair(), "crosshair should reappear instantly on release");
            CFG.cinematicZoomOutMs = 0;
            settle();
        });
        run("magnetize toggle steps strictly between defined values", () -> {
            CFG.cinematicMagnetize = true;
            CFG.cinematicSnapPoints = "2, 4, 8";
            CFG.cinematicStartingZoom = 2.0f;
            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.25);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.125);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.25);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5);

            CFG.cinematicMagnetize = false;
            CFG.cinematicSnapPoints = "";
        });
        run("disabled magnetize uses normal smooth wheel scroll even with targets set", () -> {
            CFG.cinematicMagnetize = false;
            CFG.cinematicSnapPoints = "2, 4, 8";
            CFG.cinematicStartingZoom = 2.0f;
            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5 - CFG.wheelStep);

            CFG.cinematicSnapPoints = "";
        });
        run("snap points parse zoom levels and FOVs automatically without separate mode", () -> {
            CFG.regularMagnetize = true;
            CFG.regularSnapPoints = "35, 14fov, 2x";
            ZoomManager.setBaseFov(70.0);
            CFG.regularStartingZoom = 1.0f;
            regular.down = true;
            tick();
            settle();

            // 35 is >= 20 -> 35 FOV (0.5 mul, 2x)
            // 2x -> 2x zoom (0.5 mul) - deduplicated with 35 FOV
            // 14fov -> 14 FOV (0.2 mul, 5x)
            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5); // 35 FOV

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.2); // 14 FOV

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 0.5); // back to 35 FOV

            CFG.regularMagnetize = false;
            CFG.regularSnapPoints = "";
            require(ZoomSnapPoints.stepBetweenSnaps(0.5f, java.util.List.of(0.2f, 0.5f), 0.0) == 0.5f,
                    "zero vertical should not step snaps");
            require(ZoomSnapPoints.stepBetweenSnaps(0.5f, java.util.List.of(0.2f, 0.5f), Double.NaN) == 0.5f,
                    "NaN vertical should not step snaps");
        });
        run("default settings have hide hands on and regular hide hud and hands on", () -> {
            ZoomConfig fresh = new ZoomConfig();
            require(fresh.cinematicHideHud, "cinematic hide hud should be true");
            require(fresh.cinematicHideHands, "cinematic hide hands should default to true");
            require(fresh.regularHideHud, "regular hide hud should default to true");
            require(fresh.regularHideHands, "regular hide hands should default to true");
            require(!fresh.allowUltrawide, "allow ultrawide should default to false");
            require(!fresh.preserveBarAdjustment, "preserve bar adjustment should default to false");
            require(!fresh.cinematicMagnetize, "cinematic magnetize should default to false");
            require(!fresh.regularMagnetize, "regular magnetize should default to false");
            near(fresh.maxZoomMultiplier, 1.0);
        });
        run("easing curves stay bounded and monotonic", () -> {
            for (ZoomCurve curve : ZoomCurve.values()) {
                near(curve.apply(0), 0);
                near(curve.apply(1), 1);
                double previous = 0;
                for (int i = 1; i <= 100; i++) {
                    double value = curve.apply(i / 100.0);
                    require(value >= previous && value <= 1, curve + " overshot or reversed");
                    previous = value;
                }
            }
        });
        run("arrow keys adjust bar size when zoomed in", () -> {
            CFG.cinematicBarsPercent = 15;
            CFG.preserveBarAdjustment = true;
            cinematic.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 15);
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 16);
            barsUp.down = false;
            barsDown.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 15);
        });
        run("arrow keys step by custom barAdjustStep", () -> {
            CFG.cinematicBarsPercent = 15;
            CFG.preserveBarAdjustment = true;
            CFG.barAdjustStep = 2.5f;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 17.5);
        });
        run("preserveBarAdjustment toggles config persistence", () -> {
            CFG.cinematicBarsPercent = 15;
            CFG.preserveBarAdjustment = true;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 16);
            cinematic.down = false;
            tick();
            settle();
            CFG.preserveBarAdjustment = false;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 16);
        });
        run("smoothBarChange animates while discrete snaps immediately", () -> {
            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentBarsPercent(), 15);

            CFG.smoothBarChange = false;
            require(ZoomManager.adjustBars(5.0f), "adjustBars failed");
            near(ZoomManager.getCurrentBarsPercent(), 20);

            CFG.smoothBarChange = true;
            require(ZoomManager.adjustBars(10.0f), "adjustBars failed");
            near(ZoomManager.getCurrentBarsPercent(), 20);

            Thread.sleep(20);
            ZoomManager.frameUpdate();
            float intermediate = ZoomManager.getCurrentBarsPercent();
            require(intermediate > 20.0f && intermediate < 30.0f,
                    "bars should animate smoothly between frames, got " + intermediate);

            for (int i = 0; i < 40 && Math.abs(ZoomManager.getCurrentBarsPercent() - 30.0f) > 1e-3f; i++) {
                Thread.sleep(15);
                ZoomManager.frameUpdate();
            }
            near(ZoomManager.getCurrentBarsPercent(), 30);
        });
        run("arrow keys do nothing when zoom is inactive or in regular zoom", () -> {
            CFG.cinematicBarsPercent = 15;
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 15);
            regular.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 15);
        });
        run("granular hiding controls hands, crosshair, and hud independently", () -> {
            CFG.cinematicHideHud = false;
            CFG.cinematicHideCrosshair = false;
            CFG.cinematicHideHands = true;
            cinematic.down = true;
            tick();
            settle();
            require(!ZoomManager.shouldHideHud(), "hud should not be hidden");
            require(!ZoomManager.shouldHideCrosshair(), "crosshair should not be hidden");
            require(ZoomManager.shouldHideHands(), "hands should be hidden");

            CFG.cinematicHideHands = false;
            CFG.cinematicHideCrosshair = true;
            require(!ZoomManager.shouldHideHands(), "hands should be visible");
            require(ZoomManager.shouldHideCrosshair(), "crosshair should be hidden");

            cinematic.down = false;
            tick();
            settle();
            require(!ZoomManager.shouldHideHands(), "inactive zoom should not hide hands");
            require(!ZoomManager.shouldHideCrosshair(), "inactive zoom should not hide crosshair");
            require(!ZoomManager.shouldHideHud(), "inactive zoom should not hide hud");
        });
        run("disabling arrow keys config ignores bar adjustment", () -> {
            CFG.cinematicBarsPercent = 15;
            CFG.arrowKeysAdjustBars = false;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentBarsPercent(), 15);
        });
        run("bar size adjustment stays clamped between 0 and 50", () -> {
            CFG.cinematicBarsPercent = 49.8f;
            CFG.preserveBarAdjustment = true;
            cinematic.down = true;
            tick();
            settle();
            barsUp.down = true;
            tick();
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 50);
            barsUp.down = false;
            cinematic.down = false;
            tick();
            settle();
            CFG.cinematicBarsPercent = 0.3f;
            cinematic.down = true;
            tick();
            settle();
            barsDown.down = true;
            tick();
            tick();
            settle();
            near(CFG.cinematicBarsPercent, 0);
        });
        run("debug screen hiding contract tracks shouldHideHud across activate, toggle, and release", () -> {
            CFG.cinematicHideHud = true;
            CFG.regularHideHud = false;
            require(!ZoomManager.shouldHideHud(), "inactive zoom must not hide debug screen");

            cinematic.down = true;
            tick();
            require(ZoomManager.shouldHideHud(), "cinematic hold must hide debug screen");

            cinematic.down = false;
            tick();
            require(!ZoomManager.shouldHideHud(), "release cinematic hold must restore debug screen");

            regular.down = true;
            tick();
            require(!ZoomManager.shouldHideHud(), "regular zoom with regularHideHud false must not hide debug screen");
            regular.down = false;
            tick();

            CFG.cinematicToggle = true;
            cinematic.down = true;
            tick();
            cinematic.down = false;
            tick();
            require(ZoomManager.shouldHideHud(), "toggled cinematic zoom must keep debug screen hidden");

            cinematic.down = true;
            tick();
            cinematic.down = false;
            tick();
            require(!ZoomManager.shouldHideHud(), "toggling off must restore debug screen");
        });
        run("ultrawide starting zoom under 1x activates and clamps properly", () -> {
            CFG.allowUltrawide = true;
            CFG.clamp();
            CFG.cinematicStartingZoom = 0.5f;
            CFG.clamp();
            near(CFG.cinematicStartingZoom, 0.5);

            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);
            require(ZoomManager.isZoomActive(), "ultrawide zoom should be active");

            cinematic.down = false;
            tick();
            settle();
            require(!ZoomManager.isZoomActive(), "zoom should release cleanly");
        });
        run("discrete scrolling steps across 1x ultrawide boundary", () -> {
            CFG.allowUltrawide = true;
            CFG.clamp();
            CFG.discreteScroll = true;
            CFG.discreteScrollStep = 0.5f;
            CFG.cinematicStartingZoom = 0.5f;
            CFG.clamp();

            cinematic.down = true;
            tick();
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0);

            ZoomManager.onWheel(1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0 / 1.5);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 1.0);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);

            ZoomManager.onWheel(-1);
            settle();
            near(ZoomManager.getCurrentFovMul(), 2.0);
        });
        run("legacy baseZoomMultiplier config migration", () -> {
            load("{\"baseZoomMultiplier\":0.33}");
            near(CFG.cinematicStartingZoom, 3.0);
            load("{\"baseZoomMultiplier\":0.2}");
            near(CFG.cinematicStartingZoom, 5.0);
        });
        if (failures != 0) {
            throw new AssertionError(failures + " of " + checks + " regression checks failed");
        }
        System.out.println(checks + " regression checks passed");
    }

    private static void disabledToggle(boolean regularMode) {
        CFG.cinematicToggle = !regularMode;
        CFG.regularToggle = regularMode;
        KeyMapping key = regularMode ? regular : cinematic;
        key.down = true;
        tick();
        key.down = false;
        tick();
        require(ZoomManager.isZoomActive(), "toggle was not activated");
        CFG.cinematicToggle = false;
        CFG.regularToggle = false;
        tick();
        settle();
        require(!ZoomManager.isZoomActive(), "disabled toggle left zoom active");
    }

    private static void exitProfile(boolean scale) {
        CFG.regularScaleSensitivity = scale;
        CFG.cinematicScaleSensitivity = !scale;
        CFG.regularRemoveBobbing = scale;
        CFG.cinematicRemoveBobbing = !scale;
        regular.down = true;
        tick();
        settle();
        CFG.regularZoomOutMs = 240;
        regular.down = false;
        tick();
        near(ZoomManager.getSensitivityMultiplier(), scale ? 1.0 / 3 : 1);
        require(ZoomManager.shouldRemoveBobbing() == scale, "exit used cinematic bobbing setting");
        CFG.regularZoomOutMs = 0;
    }

    private static void run(String name, Check check) throws Exception {
        ZoomManager.reset(client == null ? new Minecraft() : client);
        ZoomConfig defaults = new ZoomConfig();
        for (var field : ZoomConfig.class.getFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.set(CFG, field.get(defaults));
            }
        }
        CFG.cinematicZoomInMs = CFG.cinematicZoomOutMs = 0;
        CFG.regularZoomInMs = CFG.regularZoomOutMs = 0;
        client = new Minecraft();
        cinematic = new KeyMapping();
        regular = new KeyMapping();
        barsUp = new KeyMapping();
        barsDown = new KeyMapping();
        checks++;
        try {
            check.run();
            System.out.println("PASS: " + name);
        } catch (Throwable error) {
            failures++;
            System.err.println("FAIL: " + name + " — " + error.getMessage());
        }
    }

    private static void load(String json) throws Exception {
        Path dir = Path.of(System.getProperty("cinematiczoom.testConfig"));
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("cinematiczoom.json"), json);
        CFG.load();
    }

    private static void tick() {
        ZoomManager.tick(client, cinematic, regular, barsUp, barsDown);
    }

    private static void settle() {
        ZoomManager.frameUpdate();
        ZoomManager.frameUpdate();
    }

    private static void near(double actual, double expected) {
        require(Math.abs(actual - expected) < 0.00001, "expected " + expected + ", got " + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface Check {
        void run() throws Exception;
    }
}
