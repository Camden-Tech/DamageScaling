package me.BaddCamden.damagescaling.storage;

import me.BaddCamden.damagescaling.HealthUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Persists players' unscaled "real" health values and offers a cached view for quick retrieval.
 */
public class RealHealthStore {

    private static final String REAL_HEALTH_KEY = "real-health";

    private final Plugin plugin;
    private final Map<UUID, Double> cache = new ConcurrentHashMap<>();
    private final File dataFolder;

    /**
     * Prepares the storage helper and ensures the data directory exists for saving per-player YAML
     * files.
     *
     * @param plugin owning plugin used for logging and resolving the data folder
     */
    public RealHealthStore(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create player data directory: " + dataFolder.getAbsolutePath());
        }
    }

    /**
     * Gets a player's cached real health, loading it from the live player health as a fallback when
     * absent.
     *
     * @param player player whose health should be read
     * @return tracked real health value, clamped to the player's maximum health
     */
    public double getRealHealth(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), ignored -> HealthUtil.clamp(player.getHealth(), HealthUtil.getMaxHealth(player)));
    }

    /**
     * Updates the cached real health for a player while preventing negative values.
     *
     * @param player player being updated
     * @param value new real health value to store
     */
    public void setRealHealth(Player player, double value) {
        cache.put(player.getUniqueId(), Math.max(0.0D, value));
    }

    /**
     * Loads a player's real health from disk if available, otherwise seeds the cache with their
     * current in-game health.
     *
     * @param player player whose data should be loaded
     */
    public void load(Player player) {
        double fallback = HealthUtil.clamp(player.getHealth(), HealthUtil.getMaxHealth(player));
        File file = playerFile(player.getUniqueId());
        if (!file.exists()) {
            setRealHealth(player, fallback);
            return;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        double stored = configuration.getDouble(REAL_HEALTH_KEY, fallback);
        setRealHealth(player, stored);
    }

    /**
     * Writes a player's real health to disk, skipping work when no cache or file exists.
     *
     * @param player player whose data should be persisted
     */
    public void save(Player player) {
        File file = playerFile(player.getUniqueId());
        if (cache.isEmpty() && !file.exists()) {
            return;
        }

        double max = HealthUtil.getMaxHealth(player);
        double stored = HealthUtil.clamp(cache.getOrDefault(player.getUniqueId(), player.getHealth()), max);

        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set(REAL_HEALTH_KEY, stored);
        try {
            configuration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save real health for " + player.getName(), exception);
        }
    }

    /**
     * Saves all provided players' health data in sequence.
     *
     * @param players iterable collection of online players
     */
    public void saveAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            save(player);
        }
    }

    /**
     * Removes a player's cached entry to free memory after logout.
     *
     * @param player player whose cache entry should be cleared
     */
    public void clear(Player player) {
        cache.remove(player.getUniqueId());
    }

    /**
     * Resolves the YAML file used for persisting an individual player's data.
     *
     * @param uniqueId unique ID of the player
     * @return file handle pointing to the player's data file
     */
    private File playerFile(UUID uniqueId) {
        return new File(dataFolder, uniqueId.toString() + ".yml");
    }
}
