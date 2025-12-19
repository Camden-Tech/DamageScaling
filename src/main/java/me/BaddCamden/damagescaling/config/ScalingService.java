package me.BaddCamden.damagescaling.config;

import me.BaddCamden.damagescaling.HealthUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ScalingService {

    private static final double DEFAULT_LINEAR_FRACTION = 0.6D;
    private static final double DEFAULT_EXPONENT = 1.25D;
    private static final double DEFAULT_EXPONENTIAL_MULTIPLIER = 1.0D;
    private static final double DEFAULT_MIN_DISPLAY = 0.1D;

    private List<ScalingMode> priorities = new ArrayList<>();
    private ScalingMode fallbackMode = ScalingMode.SQUARED_DIVIDED_BY_MAX;
    private double linearFraction = DEFAULT_LINEAR_FRACTION;
    private double exponentialExponent = DEFAULT_EXPONENT;
    private double exponentialMultiplier = DEFAULT_EXPONENTIAL_MULTIPLIER;
    private double minimumDisplayHealth = DEFAULT_MIN_DISPLAY;

    public void reload(FileConfiguration configuration) {
        priorities = readPriorities(configuration);
        fallbackMode = ScalingMode.fromName(configuration.getString("scaling.mode"), ScalingMode.SQUARED_DIVIDED_BY_MAX);

        ConfigurationSection options = configuration.getConfigurationSection("scaling.options");
        if (options != null) {
            linearFraction = Math.max(0.0D, options.getConfigurationSection("linear_fraction") != null
                ? options.getConfigurationSection("linear_fraction").getDouble("fraction", DEFAULT_LINEAR_FRACTION)
                : DEFAULT_LINEAR_FRACTION);
            ConfigurationSection exponential = options.getConfigurationSection("exponential_curve");
            if (exponential != null) {
                exponentialExponent = Math.max(0.1D, exponential.getDouble("exponent", DEFAULT_EXPONENT));
                exponentialMultiplier = Math.max(0.0D, exponential.getDouble("multiplier", DEFAULT_EXPONENTIAL_MULTIPLIER));
            }
        }

        minimumDisplayHealth = Math.max(0.0D, configuration.getDouble("scaling.minimum-display-health", DEFAULT_MIN_DISPLAY));
    }

    public double computeDisplayHealth(double realHealth, double maxHealth) {
        double cappedMax = Math.max(maxHealth, 0.0001D);
        double sanitizedReal = Math.max(realHealth, 0.0D);
        ScalingMode mode = resolveMode();

        double scaled;
        switch (mode) {
            case LINEAR_FRACTION -> scaled = sanitizedReal * linearFraction;
            case EXPONENTIAL_CURVE -> {
                double normalized = Math.min(sanitizedReal, cappedMax) / cappedMax;
                scaled = cappedMax * exponentialMultiplier * Math.pow(normalized, exponentialExponent);
            }
            case SQUARED_DIVIDED_BY_MAX -> {
                scaled = (sanitizedReal * sanitizedReal) / cappedMax;
            }
            default -> scaled = sanitizedReal;
        }

        if (sanitizedReal > 0.0D && scaled < minimumDisplayHealth) {
            scaled = minimumDisplayHealth;
        }

        return HealthUtil.clamp(scaled, cappedMax);
    }

    public List<ScalingMode> getPriorities() {
        return Collections.unmodifiableList(priorities);
    }

    private List<ScalingMode> readPriorities(FileConfiguration configuration) {
        List<String> rawList = configuration.getStringList("scaling.priority");
        List<ScalingMode> parsed = new ArrayList<>();
        for (String item : rawList) {
            ScalingMode parsedMode = parseNullable(item);
            if (parsedMode != null && !parsed.contains(parsedMode)) {
                parsed.add(parsedMode);
            }
        }
        return parsed;
    }

    private ScalingMode resolveMode() {
        for (ScalingMode priority : priorities) {
            if (priority != null) {
                return priority;
            }
        }
        return fallbackMode != null ? fallbackMode : ScalingMode.SQUARED_DIVIDED_BY_MAX;
    }

    private ScalingMode parseNullable(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ScalingMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
