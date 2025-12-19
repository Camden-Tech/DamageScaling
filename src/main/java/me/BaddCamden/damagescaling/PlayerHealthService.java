package me.BaddCamden.damagescaling;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerHealthService {
    private static final String REAL_HEALTH_KEY = "real-health";

    private final DamageScalingPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Double> realHealthCache = new HashMap<>();
    private final File playerDataDirectory;

    public PlayerHealthService(DamageScalingPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerDataDirectory = new File(plugin.getDataFolder(), "playerdata");
        if (!playerDataDirectory.exists() && !playerDataDirectory.mkdirs()) {
            plugin.getLogger().warning("Could not create playerdata directory at " + playerDataDirectory.getAbsolutePath());
        }
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        double defaultHealth = getMaxHealth(player);
        double loaded = loadRealHealth(uuid, defaultHealth);
        realHealthCache.put(uuid, loaded);
        applyHealthState(player);
    }

    public void savePlayer(Player player) {
        saveRealHealth(player.getUniqueId(), getRealHealth(player));
        realHealthCache.remove(player.getUniqueId());
    }

    public void saveAllPlayers() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            saveRealHealth(online.getUniqueId(), getRealHealth(online));
        }
    }

    public double getRealHealth(Player player) {
        return realHealthCache.getOrDefault(player.getUniqueId(), clampRealHealth(player.getHealth(), player));
    }

    public void setRealHealth(Player player, double value) {
        realHealthCache.put(player.getUniqueId(), clampRealHealth(value, player));
    }

    public void applyHealthState(Player player) {
        if (configManager.isEnabled()) {
            applyScaledHealth(player);
        } else {
            safeSetHealth(player, clampRealHealth(getRealHealth(player), player));
        }
    }

    public void applyScaledHealth(Player player) {
        double realHealth = getRealHealth(player);
        double maxHealth = getMaxHealth(player);
        double scaledHealth = configManager.scale(realHealth, maxHealth);
        safeSetHealth(player, clampToPaperBounds(scaledHealth, maxHealth));
    }

    public void resetToMax(Player player) {
        double maxHealth = getMaxHealth(player);
        setRealHealth(player, maxHealth);
        applyHealthState(player);
    }

    private double clampRealHealth(double value, Player player) {
        double maxHealth = getMaxHealth(player);
        return Math.max(0.0D, Math.min(value, maxHealth));
    }

    private double clampToPaperBounds(double value, double maxHealth) {
        return Math.max(0.0D, Math.min(value, maxHealth));
    }

    private void safeSetHealth(Player player, double value) {
        if (Bukkit.isPrimaryThread()) {
            player.setHealth(value);
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> player.setHealth(value));
        }
    }

    private double loadRealHealth(UUID uuid, double defaultValue) {
        File file = playerFile(uuid);
        if (!file.exists()) {
            return defaultValue;
        }

        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
            return configuration.getDouble(REAL_HEALTH_KEY, defaultValue);
        } catch (IOException | InvalidConfigurationException exception) {
            plugin.getLogger().warning("Failed to load real health for " + uuid + ": " + exception.getMessage());
            return defaultValue;
        }
    }

    private void saveRealHealth(UUID uuid, double value) {
        File file = playerFile(uuid);
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set(REAL_HEALTH_KEY, value);
        try {
            configuration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save real health for " + uuid + ": " + exception.getMessage());
        }
    }

    private File playerFile(UUID uuid) {
        return new File(playerDataDirectory, uuid.toString() + ".yml");
    }

    public double getMaxHealth(Player player) {
        var attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null) {
            return 20.0D;
        }
        return attribute.getValue();
    }
}
