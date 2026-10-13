package mix.cinematiczoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ZoomSnapPoints {
    private ZoomSnapPoints() {
    }

    public static List<Float> parse(String raw, double baseFov, float minMul, float maxMul) {
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = raw.split(",");
        List<Float> list = new ArrayList<>();
        for (String part : parts) {
            String token = part.trim().toLowerCase();
            if (token.isEmpty()) continue;
            try {
                float val;
                boolean isFov;
                if (token.endsWith("fov")) {
                    val = Float.parseFloat(token.substring(0, token.length() - 3).trim());
                    isFov = true;
                } else if (token.endsWith("deg") || token.endsWith("°")) {
                    int cut = token.endsWith("deg") ? 3 : 1;
                    val = Float.parseFloat(token.substring(0, token.length() - cut).trim());
                    isFov = true;
                } else if (token.endsWith("x")) {
                    val = Float.parseFloat(token.substring(0, token.length() - 1).trim());
                    isFov = false;
                } else if (token.endsWith("f")) {
                    val = Float.parseFloat(token.substring(0, token.length() - 1).trim());
                    isFov = true;
                } else {
                    val = Float.parseFloat(token);
                    isFov = (val >= 20.0f);
                }
                if (val <= 0f) continue;

                float mul = isFov ? (float) (val / baseFov) : (1.0f / val);
                if (mul >= minMul - 1e-4f && mul <= maxMul + 1e-4f) {
                    mul = Math.max(minMul, Math.min(maxMul, mul));
                    if (!list.contains(mul)) {
                        list.add(mul);
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        Collections.sort(list);
        return list;
    }

    public static float stepBetweenSnaps(float current, List<Float> snaps, double vertical) {
        if (vertical == 0.0 || Double.isNaN(vertical)) return current;
        if (snaps == null || snaps.isEmpty()) {
            return current;
        }

        int notches = (int) Math.round(Math.abs(vertical));
        if (notches < 1) notches = 1;

        float result = current;
        for (int i = 0; i < notches; i++) {
            if (vertical > 0) {
                // Zooming in (smaller multiplier)
                Float nextSmaller = null;
                for (float s : snaps) {
                    if (s < result - 1e-4f) {
                        if (nextSmaller == null || s > nextSmaller) {
                            nextSmaller = s;
                        }
                    }
                }
                if (nextSmaller != null) {
                    result = nextSmaller;
                } else {
                    result = snaps.get(0);
                    break;
                }
            } else {
                // Zooming out (larger multiplier)
                Float nextLarger = null;
                for (float s : snaps) {
                    if (s > result + 1e-4f) {
                        if (nextLarger == null || s < nextLarger) {
                            nextLarger = s;
                        }
                    }
                }
                if (nextLarger != null) {
                    result = nextLarger;
                } else {
                    result = snaps.get(snaps.size() - 1);
                    break;
                }
            }
        }
        return result;
    }
}
