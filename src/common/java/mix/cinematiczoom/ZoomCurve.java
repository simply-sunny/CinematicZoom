package mix.cinematiczoom;

public enum ZoomCurve {
    EXPONENTIAL("Ease Out (Exp)"),
    EASE_OUT("Ease Out (Cubic)"),
    EASE_IN("Ease In (Cubic)"),
    EASE_IN_OUT("Ease In/Out"),
    LINEAR("Linear"),
    SINE("Sine");

    private final String displayName;

    ZoomCurve(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public double apply(double t) {
        if (t <= 0.0) return 0.0;
        if (t >= 1.0) return 1.0;

        switch (this) {
            case LINEAR:
                return t;
            case EASE_IN:
                return t * t * t;
            case EASE_OUT:
                double inv = 1.0 - t;
                return 1.0 - inv * inv * inv;
            case EASE_IN_OUT:
                return t < 0.5 ? 4.0 * t * t * t : 1.0 - Math.pow(-2.0 * t + 2.0, 3) / 2.0;
            case SINE:
                return Math.sin(t * (Math.PI / 2.0));
            case EXPONENTIAL:
            default:
                double k = 4.605170185988092; // ln(100) -> reaches ~99% at t=1
                return (1.0 - Math.exp(-k * t)) / (1.0 - Math.exp(-k));
        }
    }
}
