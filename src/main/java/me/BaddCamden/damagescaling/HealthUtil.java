package me.BaddCamden.damagescaling;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class HealthUtil {

    private HealthUtil() {
    }

    public static double getMaxHealth(Player player) {
        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance == null) {
            return 20.0D;
        }
        return instance.getValue();
    }

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
