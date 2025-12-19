package me.BaddCamden.damagescaling;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final DamageScalingPlugin plugin;
    private boolean enabled;
    private ScalingMode scalingMode;
    private double linearMultiplier;
    private double exponentialExponent;
    private double flatValue;

    public ConfigManager(DamageScalingPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("enabled", true);
        this.scalingMode = ScalingMode.fromConfig(config.getString("scaling-mode"));
        this.linearMultiplier = config.getDouble("linear.multiplier", 1.0D);
        this.exponentialExponent = config.getDouble("exponential.exponent", 1.5D);
        this.flatValue = config.getDouble("flat.value", 20.0D);
    }

    public void saveEnabledState(boolean newEnabled) {
        this.enabled = newEnabled;
        plugin.getConfig().set("enabled", newEnabled);
        plugin.saveConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ScalingMode getScalingMode() {
        return scalingMode;
    }

    public double getLinearMultiplier() {
        return linearMultiplier;
    }

    public double getExponentialExponent() {
        return exponentialExponent;
    }

    public double getFlatValue() {
        return flatValue;
    }

    public double scale(double realHealth, double maxHealth) {
        maxHealth = Math.max(0.0001D, maxHealth);
        return switch (scalingMode) {
            case LINEAR -> realHealth * linearMultiplier;
            case EXPONENTIAL -> Math.pow(realHealth / maxHealth, exponentialExponent) * maxHealth;
            case FLAT -> flatValue;
            case SQUARE_DIVIDED_BY_MAX -> (realHealth * realHealth) / maxHealth;
        };
    }
}
