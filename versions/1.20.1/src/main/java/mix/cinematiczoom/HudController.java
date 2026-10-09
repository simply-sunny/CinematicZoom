package mix.cinematiczoom;

import net.minecraft.client.MinecraftClient;

final class HudController {
    private static boolean hiddenByUs;

    private HudController() {
    }

    static void setHidden(MinecraftClient client, boolean hideHud) {
        hiddenByUs = hideHud;
    }

    static boolean shouldHideHud() {
        return hiddenByUs;
    }
}
