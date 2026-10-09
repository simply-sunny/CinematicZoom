package mix.cinematiczoom;

import net.minecraft.client.MinecraftClient;

final class HudController {
    private static boolean hiddenByUs;

    private HudController() {
    }

    static void setHidden(MinecraftClient client, boolean hideHud) {
        hiddenByUs = hideHud;
    }

    static void acquire(MinecraftClient client) {
        hiddenByUs = true;
    }

    static void release(MinecraftClient client) {
        hiddenByUs = false;
    }

    static boolean shouldHideHud() {
        return hiddenByUs;
    }
}
