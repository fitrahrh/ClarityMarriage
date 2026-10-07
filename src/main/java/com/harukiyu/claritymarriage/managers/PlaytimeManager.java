package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks shared playtime of couples when both partners are online simultaneously.
 */
public class PlaytimeManager {

    private final ClarityMarriage plugin;
    private BukkitTask task;
    private int counter = 0;

    public PlaytimeManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        // Run every 60 seconds (1200 ticks)
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tickPlaytime, 1200L, 1200L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tickPlaytime() {
        Set<UUID> processedMarriages = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
            if (marriage == null || processedMarriages.contains(marriage.getId())) {
                continue;
            }

            UUID partnerUuid = marriage.getPartner(player.getUniqueId());
            Player partner = Bukkit.getPlayer(partnerUuid);

            if (partner != null && partner.isOnline()) {
                marriage.addPlaytime(60);
                processedMarriages.add(marriage.getId());
            }
        }

        // Auto-save playtime every 5 minutes
        counter++;
        if (counter >= 5) {
            counter = 0;
            plugin.getMarriageManager().saveAll();
        }
    }
}
