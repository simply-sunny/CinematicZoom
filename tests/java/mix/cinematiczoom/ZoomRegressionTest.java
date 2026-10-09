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
            cinematic.down = true;
            tick();
            cinematic.down = false;
            regular.down = true;
            tick();
            require(!client.gui.hud.isHidden(), "regular mode should restore HUD");
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
            require(client.gui.hud.isHidden(), "cinematic profile did not hide HUD");
        });
        run("hold zoom stops when a screen opens", () -> {
            cinematic.down = true;
            tick();
            client.gui.screen = new Object();
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
            near(ZoomManager.getCurrentFovMul(), 1);
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
            near(ZoomManager.getCurrentFovMul(), 1);
            regular.down = false;
            tick();
            require(!ZoomManager.onWheel(1), "inactive wheel event was consumed");
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
        checks++;
        try {
            check.run();
            System.out.println("PASS: " + name);
        } catch (AssertionError error) {
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
        ZoomManager.tick(client, cinematic, regular);
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
