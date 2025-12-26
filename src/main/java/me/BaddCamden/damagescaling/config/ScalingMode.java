package me.BaddCamden.damagescaling.config;

import java.util.Locale;

/**
 * Enumerates supported methods for translating real health values into displayed hearts.
 */
public enum ScalingMode {
    /** Scales by squaring real health and dividing by the player's maximum health. */
    SQUARED_DIVIDED_BY_MAX,
    /** Scales linearly based on a configured fraction of real health. */
    LINEAR_FRACTION,
    /** Uses an exponential curve to emphasize early damage or healing. */
    EXPONENTIAL_CURVE;

    /**
     * Attempts to parse a scaling mode from configuration-friendly text, falling back when the
     * provided value is invalid.
     *
     * @param name     input string to parse
     * @param fallback default mode returned when parsing fails
     * @return resolved {@link ScalingMode}
     */
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
