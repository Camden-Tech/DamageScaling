package me.BaddCamden.damagescaling.config;

import java.util.Locale;

public enum ScalingMode {
    SQUARED_DIVIDED_BY_MAX,
    LINEAR_FRACTION,
    EXPONENTIAL_CURVE;

    public static ScalingMode fromName(String name, ScalingMode fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return ScalingMode.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
