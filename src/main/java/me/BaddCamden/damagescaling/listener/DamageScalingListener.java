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

/**
 * Listens for player lifecycle and combat events to keep real and visual health values in sync.
 */
public class DamageScalingListener implements Listener {

    private final DamageScalingPlugin plugin;
    private final RealHealthStore realHealthStore;
    private final ScalingService scalingService;

    /**
     * Builds a listener bound to the plugin's services so health changes can be coordinated.
     *
     * @param plugin owning plugin instance
     */
    public DamageScalingListener(DamageScalingPlugin plugin) {
        this.plugin = plugin;
        this.realHealthStore = plugin.getRealHealthStore();
        this.scalingService = plugin.getScalingService();
    }

    /**
     * Loads a joining player's stored health and applies the appropriate visual value.
     *
     * @param event player join event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        realHealthStore.load(player);
        applyVisualHealth(plugin, player);
    }

    /**
     * Persists and clears a quitting player's real health from the cache.
     *
     * @param event player quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        realHealthStore.save(player);
        realHealthStore.clear(player);
    }

    /**
     * Overrides incoming damage to adjust the stored real health and optionally display scaled
     * values depending on whether the feature is enabled.
     *
     * @param event entity damage event affecting a player
     */
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

    /**
     * Captures healing events to raise the stored real health and keep the player's visible health
     * aligned with the selected scaling mode.
     *
     * @param event entity regain health event affecting a player
     */
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

    /**
     * Records a player's death by zeroing their stored real health before persistence.
     *
     * @param event player death event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        realHealthStore.setRealHealth(player, 0.0D);
        realHealthStore.save(player);
    }

    /**
     * Restores a respawning player's real health to their maximum before reapplying visual scaling.
     *
     * @param event player respawn event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            double maxHealth = HealthUtil.getMaxHealth(player);
            realHealthStore.setRealHealth(player, maxHealth);
            applyVisualHealth(plugin, player);
        });
    }

    /**
     * Synchronizes a player's displayed health hearts with their stored real health using the
     * current scaling rules.
     *
     * @param plugin plugin providing scaling state and storage access
     * @param player player whose hearts should be updated
     */
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
