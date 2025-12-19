package me.BaddCamden.damagescaling;

public enum ScalingMode {
    SQUARE_DIVIDED_BY_MAX,
    LINEAR,
    EXPONENTIAL,
    FLAT;

    public static ScalingMode fromConfig(String value) {
        if (value == null) {
            return SQUARE_DIVIDED_BY_MAX;
        }
        try {
            return ScalingMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SQUARE_DIVIDED_BY_MAX;
        }
    }
}
