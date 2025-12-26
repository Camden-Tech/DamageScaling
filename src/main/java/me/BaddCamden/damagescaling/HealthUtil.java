package me.BaddCamden.damagescaling;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

/**
 * Utility methods for safely handling player health values without assuming Bukkit always returns
 * non-null attributes.
 */
public final class HealthUtil {

    /**
     * Hidden constructor to prevent instantiation of this static utility class.
     */
    private HealthUtil() {
    }

    /**
     * Retrieves the maximum health value available to a player, falling back to the vanilla default
     * of 20.0 hearts if the attribute is unavailable.
     *
     * @param player player whose health cap should be inspected
     * @return the numeric maximum health value to use for calculations
     */
    public static double getMaxHealth(Player player) {
        AttributeInstance instance = player.getAttribute(Attribute.MAX_HEALTH);
        if (instance == null) {
            return 20.0D;
        }
        return instance.getValue();
    }

    /**
     * Restricts a health value to the inclusive range {@code [0, max]} while ensuring the upper
     * bound itself is not negative.
     *
     * @param value health value to sanitize
     * @param max   desired upper bound; negative values are treated as zero
     * @return the clamped value safe to apply to Bukkit health setters
     */
    public static double clamp(double value, double max) {
        double upperBound = Math.max(max, 0.0D);
        if (value < 0.0D) {
            return 0.0D;
        }
        if (value > upperBound) {
            return upperBound;
        }
        return value;
    }
}
