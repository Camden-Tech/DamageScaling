package me.BaddCamden.damagescaling;

import me.BaddCamden.damagescaling.command.DamageScalingCommand;
import me.BaddCamden.damagescaling.listener.DamageScalingListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DamageScalingPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private PlayerHealthService playerHealthService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.playerHealthService = new PlayerHealthService(this, configManager);

        var listener = new DamageScalingListener(playerHealthService, configManager);
        Bukkit.getPluginManager().registerEvents(listener, this);

        var command = getCommand("damagescaling");
        if (command != null) {
            var executor = new DamageScalingCommand(configManager, playerHealthService);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("Command /damagescaling is not defined in plugin.yml");
        }

        Bukkit.getOnlinePlayers().forEach(playerHealthService::loadPlayer);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(playerHealthService::applyHealthState);
        playerHealthService.saveAllPlayers();
    }
}
