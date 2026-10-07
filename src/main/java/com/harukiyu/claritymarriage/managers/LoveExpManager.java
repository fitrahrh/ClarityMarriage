package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the EXP Love progression system, proximity detection, and love interactions.
 */
public class LoveExpManager {

    private final ClarityMarriage plugin;
    private final Map<UUID, Long> kissCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> proximityTimeCounters = new ConcurrentHashMap<>();
    private BukkitTask task;

    public LoveExpManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        // Ticks every 20 seconds (400 ticks) to update proximity playtime counters
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tickProximityPlaytime, 400L, 400L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        kissCooldowns.clear();
        proximityTimeCounters.clear();
    }

    /**
     * Checks if a player is close to their married partner.
     */
    public boolean isNearPartner(Player player, double maxDistance) {
        if (player == null || !player.isOnline()) return false;
        Player partner = plugin.getMarriageManager().getPartnerPlayer(player.getUniqueId());
        if (partner == null || !partner.isOnline()) return false;

        Location l1 = player.getLocation();
        Location l2 = partner.getLocation();

        if (l1.getWorld() == null || l2.getWorld() == null || !l1.getWorld().equals(l2.getWorld())) {
            return false;
        }

        return l1.distanceSquared(l2) <= (maxDistance * maxDistance);
    }

    /**
     * Awards EXP Love to a married couple.
     */
    public void awardLoveExp(Marriage marriage, long amount, String reasonName, Player causePlayer) {
        if (marriage == null || amount <= 0) return;

        marriage.addLoveExp(amount);
        plugin.getStorageManager().saveMarriage(marriage);

        Player p1 = Bukkit.getPlayer(marriage.getPlayer1());
        Player p2 = Bukkit.getPlayer(marriage.getPlayer2());

        // Play particle and sound effects for online partners
        if (p1 != null && p1.isOnline()) {
            notifyExpGain(p1, amount, reasonName, marriage.getLoveExp());
        }
        if (p2 != null && p2.isOnline()) {
            notifyExpGain(p2, amount, reasonName, marriage.getLoveExp());
        }
    }

    private void notifyExpGain(Player player, long amount, String reason, long total) {
        if (plugin.getConfigManager().isPlaySounds()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundLoveExpGain(), 0.8f, 1.4f);
        }
        if (plugin.getConfigManager().isSpawnParticles()) {
            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 1.2, 0), 4, 0.3, 0.3, 0.3, 0.05);
        }
        plugin.getLanguageManager().sendMessage(player, "love-exp-gain",
                "amount", String.valueOf(amount),
                "reason", reason,
                "total", String.valueOf(total)
        );
    }

    /**
     * Handles the sweet kiss / hug interaction between partners.
     */
    public void handleKiss(Player player, Player partner) {
        UUID uuid = player.getUniqueId();
        int cdSeconds = plugin.getConfigManager().getLoveKissCooldown();
        long lastKiss = kissCooldowns.getOrDefault(uuid, 0L);
        long elapsed = (System.currentTimeMillis() - lastKiss) / 1000L;

        if (elapsed < cdSeconds) {
            long remaining = cdSeconds - elapsed;
            plugin.getLanguageManager().sendMessage(player, "love-kiss-cooldown", "time", String.valueOf(remaining));
            return;
        }

        Marriage marriage = plugin.getMarriageManager().getMarriage(uuid);
        if (marriage == null) return;

        kissCooldowns.put(uuid, System.currentTimeMillis());
        int exp = plugin.getConfigManager().getLoveKissExp();
        marriage.addLoveExp(exp);
        plugin.getStorageManager().saveMarriage(marriage);

        // Visual and sound effects
        if (plugin.getConfigManager().isPlaySounds()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundKiss(), 1.0f, 1.0f);
            partner.playSound(partner.getLocation(), plugin.getConfigManager().getSoundKiss(), 1.0f, 1.0f);
        }
        if (plugin.getConfigManager().isSpawnParticles()) {
            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 15, 0.5, 0.5, 0.5, 0.1);
            partner.spawnParticle(Particle.HEART, partner.getLocation().add(0, 1.5, 0), 15, 0.5, 0.5, 0.5, 0.1);
        }

        plugin.getLanguageManager().sendMessage(player, "love-kiss-success",
                "partner", partner.getName(),
                "amount", String.valueOf(exp)
        );
        plugin.getLanguageManager().sendMessage(partner, "love-kiss-partner",
                "partner", player.getName(),
                "amount", String.valueOf(exp)
        );
    }

    private void tickProximityPlaytime() {
        Set<UUID> checkedMarriages = new HashSet<>();
        double maxDist = plugin.getConfigManager().getLoveProximityDistance();
        int intervalSeconds = plugin.getConfigManager().getLovePlaytimeInterval();
        int expReward = plugin.getConfigManager().getLovePlaytimeExp();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Marriage marriage = plugin.getMarriageManager().getMarriage(player.getUniqueId());
            if (marriage == null || checkedMarriages.contains(marriage.getId())) {
                continue;
            }

            if (isNearPartner(player, maxDist)) {
                checkedMarriages.add(marriage.getId());
                int seconds = proximityTimeCounters.getOrDefault(marriage.getId(), 0) + 20;

                if (seconds >= intervalSeconds) {
                    proximityTimeCounters.put(marriage.getId(), 0);
                    awardLoveExp(marriage, expReward, "Proximity Playtime", player);
                } else {
                    proximityTimeCounters.put(marriage.getId(), seconds);
                }
            } else {
                proximityTimeCounters.remove(marriage.getId());
            }
        }
    }
}
