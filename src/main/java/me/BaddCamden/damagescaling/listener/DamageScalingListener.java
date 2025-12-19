package me.BaddCamden.damagescaling.listener;

import me.BaddCamden.damagescaling.ConfigManager;
import me.BaddCamden.damagescaling.PlayerHealthService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DamageScalingListener implements Listener {
    private final PlayerHealthService playerHealthService;
    private final ConfigManager configManager;

    public DamageScalingListener(PlayerHealthService playerHealthService, ConfigManager configManager) {
        this.playerHealthService = playerHealthService;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerHealthService.loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerHealthService.savePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        playerHealthService.resetToMax(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        double current = playerHealthService.getRealHealth(player);
        double newHealth = Math.max(0.0D, current - event.getFinalDamage());
        playerHealthService.setRealHealth(player, newHealth);

        if (!configManager.isEnabled()) {
            return;
        }

        if (newHealth <= 0.0D) {
            player.setHealth(0.0D);
        } else {
            playerHealthService.applyScaledHealth(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        double current = playerHealthService.getRealHealth(player);
        double maxHealth = playerHealthService.getMaxHealth(player);
        double newHealth = Math.min(maxHealth, current + event.getAmount());
        playerHealthService.setRealHealth(player, newHealth);

        if (configManager.isEnabled()) {
            playerHealthService.applyScaledHealth(player);
        }
    }
}
