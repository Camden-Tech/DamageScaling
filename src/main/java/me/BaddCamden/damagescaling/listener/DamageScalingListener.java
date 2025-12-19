package me.BaddCamden.damagescaling.listener;

import me.BaddCamden.damagescaling.DamageScalingPlugin;
import me.BaddCamden.damagescaling.HealthUtil;
import me.BaddCamden.damagescaling.config.ScalingService;
import me.BaddCamden.damagescaling.storage.RealHealthStore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DamageScalingListener implements Listener {

    private final DamageScalingPlugin plugin;
    private final RealHealthStore realHealthStore;
    private final ScalingService scalingService;

    public DamageScalingListener(DamageScalingPlugin plugin) {
        this.plugin = plugin;
        this.realHealthStore = plugin.getRealHealthStore();
        this.scalingService = plugin.getScalingService();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        realHealthStore.load(player);
        applyVisualHealth(plugin, player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        realHealthStore.save(player);
        realHealthStore.clear(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        double maxHealth = HealthUtil.getMaxHealth(player);
        if (!plugin.isScalingEnabled()) {
            double predicted = Math.max(0.0D, HealthUtil.clamp(player.getHealth(), maxHealth) - event.getFinalDamage());
            realHealthStore.setRealHealth(player, predicted);
            return;
        }

        double realHealth = realHealthStore.getRealHealth(player);
        double updated = Math.max(0.0D, realHealth - event.getFinalDamage());

        realHealthStore.setRealHealth(player, updated);
        if (updated <= 0.0D) {
            player.setHealth(0.0D);
            event.setCancelled(true);
            event.setDamage(0.0D);
            return;
        }

        event.setDamage(0.0D);
        double display = scalingService.computeDisplayHealth(updated, maxHealth);
        player.setHealth(display);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        double maxHealth = HealthUtil.getMaxHealth(player);
        if (!plugin.isScalingEnabled()) {
            double predicted = Math.min(maxHealth, HealthUtil.clamp(player.getHealth(), maxHealth) + event.getAmount());
            realHealthStore.setRealHealth(player, predicted);
            return;
        }

        double realHealth = realHealthStore.getRealHealth(player);
        double healed = Math.min(maxHealth, realHealth + event.getAmount());

        realHealthStore.setRealHealth(player, healed);
        event.setAmount(0.0D);
        double display = scalingService.computeDisplayHealth(healed, maxHealth);
        player.setHealth(display);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        realHealthStore.setRealHealth(player, 0.0D);
        realHealthStore.save(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            double maxHealth = HealthUtil.getMaxHealth(player);
            realHealthStore.setRealHealth(player, maxHealth);
            applyVisualHealth(plugin, player);
        });
    }

    public static void applyVisualHealth(DamageScalingPlugin plugin, Player player) {
        double maxHealth = HealthUtil.getMaxHealth(player);
        RealHealthStore store = plugin.getRealHealthStore();
        double realHealth = store.getRealHealth(player);

        if (!plugin.isScalingEnabled()) {
            double clamped = HealthUtil.clamp(realHealth, maxHealth);
            if (player.getHealth() != clamped) {
                player.setHealth(clamped);
            }
            return;
        }

        double display = plugin.getScalingService().computeDisplayHealth(realHealth, maxHealth);
        player.setHealth(display);
    }
}
