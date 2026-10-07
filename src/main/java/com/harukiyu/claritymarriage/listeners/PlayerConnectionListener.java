package com.harukiyu.claritymarriage.listeners;

import com.harukiyu.claritymarriage.ClarityMarriage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Listens for player join and quit events to send notifications and clean up session caches.
 */
public class PlayerConnectionListener implements Listener {

    private final ClarityMarriage plugin;

    public PlayerConnectionListener(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.getConfigManager().isPartnerJoinNotify()) {
            Player partner = plugin.getMarriageManager().getPartnerPlayer(uuid);
            if (partner != null && partner.isOnline()) {
                plugin.getLanguageManager().sendMessage(partner, "partner-joined", "partner", player.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        plugin.getTeleportManager().cleanupPlayer(uuid);
        plugin.getChatManager().disableChat(uuid);
        plugin.getProposalManager().removeProposalsFor(uuid);
    }
}
