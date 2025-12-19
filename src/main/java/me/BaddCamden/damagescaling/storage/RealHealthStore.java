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

public class RealHealthStore {

    private static final String REAL_HEALTH_KEY = "real-health";

    private final Plugin plugin;
    private final Map<UUID, Double> cache = new ConcurrentHashMap<>();
    private final File dataFolder;

    public RealHealthStore(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create player data directory: " + dataFolder.getAbsolutePath());
        }
    }

    public double getRealHealth(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), ignored -> HealthUtil.clamp(player.getHealth(), HealthUtil.getMaxHealth(player)));
    }

    public void setRealHealth(Player player, double value) {
        cache.put(player.getUniqueId(), Math.max(0.0D, value));
    }

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

    public void saveAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            save(player);
        }
    }

    public void clear(Player player) {
        cache.remove(player.getUniqueId());
    }

    private File playerFile(UUID uniqueId) {
        return new File(dataFolder, uniqueId.toString() + ".yml");
    }
}
