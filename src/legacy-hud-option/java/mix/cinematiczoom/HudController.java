package mix.cinematiczoom;

import net.minecraft.client.MinecraftClient;

final class HudController {
    private static boolean hiddenByUs;

    private HudController() {
    }

    static void setHidden(MinecraftClient client, boolean hideHud) {
        if (hideHud) {
            if (!client.options.hudHidden) {
                client.options.hudHidden = true;
                hiddenByUs = true;
            }
        } else if (hiddenByUs) {
            client.options.hudHidden = false;
            hiddenByUs = false;
        }
    }

    static boolean shouldHideHud() {
        return false;
    }
}
