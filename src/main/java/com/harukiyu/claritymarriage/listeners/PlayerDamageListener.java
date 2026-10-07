package com.harukiyu.claritymarriage.listeners;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Handles damage events for teleport cancellation, combat tagging, and couple PvP protection.
 */
public class PlayerDamageListener implements Listener {

    private final ClarityMarriage plugin;

    public PlayerDamageListener(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Cancel teleport warmup on taking damage
        if (plugin.getConfigManager().isTeleportCancelOnDamage() && plugin.getTeleportManager().isWarmingUp(player.getUniqueId())) {
            plugin.getTeleportManager().cancelWarmup(player.getUniqueId(), "tp-cancelled-damage");
        }

        // Combat tag victim
        plugin.getTeleportManager().tagCombat(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player damager = null;
        if (event.getDamager() instanceof Player p) {
            damager = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            damager = p;
        }

        if (damager == null) {
            return;
        }

        // Combat tag damager
        plugin.getTeleportManager().tagCombat(damager.getUniqueId());

        // Check couple PvP protection
        Marriage marriage = plugin.getMarriageManager().getMarriage(victim.getUniqueId());
        if (marriage != null && marriage.contains(damager.getUniqueId())) {
            if (!plugin.getConfigManager().isAllowPvp() && !marriage.isPvpEnabled()) {
                event.setCancelled(true);
                plugin.getLanguageManager().sendMessage(damager, "pvp-blocked");
            }
        }
    }
}
