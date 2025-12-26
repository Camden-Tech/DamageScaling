package me.BaddCamden.damagescaling;

import me.BaddCamden.damagescaling.command.DamageScalingCommand;
import me.BaddCamden.damagescaling.config.ScalingService;
import me.BaddCamden.damagescaling.listener.DamageScalingListener;
import me.BaddCamden.damagescaling.storage.RealHealthStore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main entry point for the DamageScaling plugin, wiring services, listeners, and configuration
 * management together.
 */
public class DamageScalingPlugin extends JavaPlugin {

    private RealHealthStore realHealthStore;
    private ScalingService scalingService;
    private boolean scalingEnabled;

    /**
     * Initializes the plugin, creating core services, loading configuration, and applying scaling
     * visuals to any online players.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        realHealthStore = new RealHealthStore(this);
        scalingService = new ScalingService();

        reloadSettings();
        registerListeners();
        registerCommands();
        refreshAllPlayers();
    }

    /**
     * Cleans up plugin state by restoring players' real health and persisting cached values.
     */
    @Override
    public void onDisable() {
        restoreRealHealth();
        realHealthStore.saveAll(Bukkit.getOnlinePlayers());
    }

    /**
     * Indicates whether the plugin should apply scaled visual health or respect vanilla damage.
     *
     * @return {@code true} when scaling is active
     */
    public boolean isScalingEnabled() {
        return scalingEnabled;
    }

    /**
     * Toggles the scaling feature and persists the setting to {@code config.yml}.
     *
     * @param enabled desired enabled state
     */
    public void setScalingEnabled(boolean enabled) {
        this.scalingEnabled = enabled;
        getConfig().set("enabled", enabled);
        saveConfig();
    }

    /**
     * Provides access to the scaling configuration service for listeners and commands.
     *
     * @return the shared {@link ScalingService}
     */
    public ScalingService getScalingService() {
        return scalingService;
    }

    /**
     * Exposes the real health storage helper for coordinating cached values.
     *
     * @return the shared {@link RealHealthStore}
     */
    public RealHealthStore getRealHealthStore() {
        return realHealthStore;
    }

    /**
     * Reloads configuration from disk and refreshes the scaling service with the latest options.
     */
    public void reloadSettings() {
        reloadConfig();
        FileConfiguration config = getConfig();
        this.scalingEnabled = config.getBoolean("enabled", true);
        scalingService.reload(config);
    }

    /**
     * Ensures all online players have their real health loaded and their display health synced with
     * the current scaling mode.
     */
    public void refreshAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            realHealthStore.load(player);
            DamageScalingListener.applyVisualHealth(this, player);
        }
    }

    /**
     * Applies stored real health values back to each online player, undoing visual scaling.
     */
    public void restoreRealHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            double max = HealthUtil.getMaxHealth(player);
            double real = realHealthStore.getRealHealth(player);
            player.setHealth(HealthUtil.clamp(real, max));
        }
    }

    /**
     * Registers the event listeners responsible for scaling damage and health changes.
     */
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new DamageScalingListener(this), this);
    }

    /**
     * Hooks the {@code /damagescaling} command into Bukkit's command map.
     */
    private void registerCommands() {
        DamageScalingCommand command = new DamageScalingCommand(this);
        if (getCommand("damagescaling") == null) {
            getLogger().severe("Command 'damagescaling' is missing from plugin.yml");
            return;
        }
        getCommand("damagescaling").setExecutor(command);
        getCommand("damagescaling").setTabCompleter(command);
    }
}
