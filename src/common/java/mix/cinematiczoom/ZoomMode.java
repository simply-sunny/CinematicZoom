package mix.cinematiczoom;

public enum ZoomMode {
    NONE,
    CINEMATIC,
    REGULAR;

    public boolean isActive() {
        return this != NONE;
    }
}
