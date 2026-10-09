package mix.cinematiczoom;

final class ZoomInputState {
    private boolean cinematicToggled;
    private boolean regularToggled;
    private boolean wasCinematicDown;
    private boolean wasRegularDown;

    ZoomMode update(boolean inWorld, boolean cinematicDown, boolean regularDown) {
        if (!inWorld) {
            reset();
            return ZoomMode.NONE;
        }

        ZoomConfig cfg = ZoomConfig.INSTANCE;
        if (!cfg.cinematicToggle) cinematicToggled = false;
        if (!cfg.regularToggle) regularToggled = false;

        if (cfg.cinematicToggle && cinematicDown && !wasCinematicDown) {
            cinematicToggled = !cinematicToggled;
            if (cinematicToggled) regularToggled = false;
        }
        if (cfg.regularToggle && regularDown && !wasRegularDown) {
            regularToggled = !regularToggled;
            if (regularToggled) cinematicToggled = false;
        }
        wasCinematicDown = cinematicDown;
        wasRegularDown = regularDown;

        if (cinematicToggled) return ZoomMode.CINEMATIC;
        if (regularToggled) return ZoomMode.REGULAR;
        if (!cfg.cinematicToggle && cinematicDown) return ZoomMode.CINEMATIC;
        if (!cfg.regularToggle && regularDown) return ZoomMode.REGULAR;
        return ZoomMode.NONE;
    }

    void reset() {
        cinematicToggled = false;
        regularToggled = false;
        wasCinematicDown = false;
        wasRegularDown = false;
    }
}
