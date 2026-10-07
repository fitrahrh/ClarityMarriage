package com.harukiyu.claritymarriage.listeners;

import com.harukiyu.claritymarriage.ClarityMarriage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens for player movement to cancel teleport warmups if configured.
 */
public class PlayerMoveListener implements Listener {

    private final ClarityMarriage plugin;

    public PlayerMoveListener(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().isTeleportCancelOnMove()) {
            return;
        }

        Player player = event.getPlayer();
        if (!plugin.getTeleportManager().isWarmingUp(player.getUniqueId())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            plugin.getTeleportManager().cancelWarmup(player.getUniqueId(), "tp-cancelled-movement");
        }
    }
}
