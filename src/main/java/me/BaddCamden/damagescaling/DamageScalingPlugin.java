package me.BaddCamden.damagescaling;

import me.BaddCamden.damagescaling.command.DamageScalingCommand;
import me.BaddCamden.damagescaling.config.ScalingService;
import me.BaddCamden.damagescaling.listener.DamageScalingListener;
import me.BaddCamden.damagescaling.storage.RealHealthStore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DamageScalingPlugin extends JavaPlugin {

    private RealHealthStore realHealthStore;
    private ScalingService scalingService;
    private boolean scalingEnabled;

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

    @Override
    public void onDisable() {
        restoreRealHealth();
        realHealthStore.saveAll(Bukkit.getOnlinePlayers());
    }

    public boolean isScalingEnabled() {
        return scalingEnabled;
    }

    public void setScalingEnabled(boolean enabled) {
        this.scalingEnabled = enabled;
        getConfig().set("enabled", enabled);
        saveConfig();
    }

    public ScalingService getScalingService() {
        return scalingService;
    }

    public RealHealthStore getRealHealthStore() {
        return realHealthStore;
    }

    public void reloadSettings() {
        reloadConfig();
        FileConfiguration config = getConfig();
        this.scalingEnabled = config.getBoolean("enabled", true);
        scalingService.reload(config);
    }

    public void refreshAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            realHealthStore.load(player);
            DamageScalingListener.applyVisualHealth(this, player);
        }
    }

    public void restoreRealHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            double max = HealthUtil.getMaxHealth(player);
            double real = realHealthStore.getRealHealth(player);
            player.setHealth(HealthUtil.clamp(real, max));
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new DamageScalingListener(this), this);
    }

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
