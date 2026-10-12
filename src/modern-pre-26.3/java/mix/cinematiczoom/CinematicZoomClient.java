package mix.cinematiczoom;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class CinematicZoomClient implements ClientModInitializer {

    private static final String MODID = "cinematiczoom";
    public static ZoomKeyMapping ZOOM_KEYBIND;
    public static ZoomKeyMapping ZOOM_NO_BARS_KEYBIND;
    public static ZoomKeyMapping INCREASE_BARS_KEYBIND;
    public static ZoomKeyMapping DECREASE_BARS_KEYBIND;

    public static ZoomKeyMapping[] getKeyMappings() {
        if (ZOOM_KEYBIND == null) {
            KeyMapping.Category category = KeyMapping.Category.register(Identifier.parse(MODID + ":cinematiczoom"));
            ZOOM_KEYBIND = new ZoomKeyMapping(
                    "key.cinematiczoom.zoom",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_C,
                    category
            );
            ZOOM_NO_BARS_KEYBIND = new ZoomKeyMapping(
                    "key.cinematiczoom.zoom_no_bars",
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    category
            );
            INCREASE_BARS_KEYBIND = new ZoomKeyMapping(
                    "key.cinematiczoom.increase_bars",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_UP,
                    category
            );
            DECREASE_BARS_KEYBIND = new ZoomKeyMapping(
                    "key.cinematiczoom.decrease_bars",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_DOWN,
                    category
            );
        }
        return new ZoomKeyMapping[] {
                ZOOM_KEYBIND,
                ZOOM_NO_BARS_KEYBIND,
                INCREASE_BARS_KEYBIND,
                DECREASE_BARS_KEYBIND
        };
    }

    @Override
    public void onInitializeClient() {
        ZoomConfig.INSTANCE.load();
        getKeyMappings();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ZoomManager.reset(client));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ZoomManager.reset(client);
            ZoomConfig.INSTANCE.save();
        });
    }
}
