package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.SharedHome;
import com.harukiyu.claritymarriage.utils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles teleportation with warmup countdown, movement/damage cancellation, and combat tagging.
 */
public class TeleportManager {

    private final ClarityMarriage plugin;
    private final Map<UUID, Long> teleportCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> pendingTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Location> initialLocations = new ConcurrentHashMap<>();

    public TeleportManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public void tagCombat(UUID playerUuid) {
        combatTags.put(playerUuid, System.currentTimeMillis());
    }

    public boolean isCombatTagged(UUID playerUuid) {
        long last = combatTags.getOrDefault(playerUuid, 0L);
        int tagSeconds = plugin.getConfigManager().getCombatTagSeconds();
        return (System.currentTimeMillis() - last) < (tagSeconds * 1000L);
    }

    public long getCombatTimeRemaining(UUID playerUuid) {
        long last = combatTags.getOrDefault(playerUuid, 0L);
        long elapsed = (System.currentTimeMillis() - last) / 1000L;
        int tagSeconds = plugin.getConfigManager().getCombatTagSeconds();
        return Math.max(0, tagSeconds - elapsed);
    }

    public boolean isWarmingUp(UUID playerUuid) {
        return pendingTasks.containsKey(playerUuid);
    }

    public Location getInitialLocation(UUID playerUuid) {
        return initialLocations.get(playerUuid);
    }

    public void cancelWarmup(UUID playerUuid, String reasonLangKey) {
        BukkitTask task = pendingTasks.remove(playerUuid);
        initialLocations.remove(playerUuid);
        if (task != null) {
            task.cancel();
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && reasonLangKey != null) {
                plugin.getLanguageManager().sendMessage(player, reasonLangKey);
            }
        }
    }

    public void cleanupPlayer(UUID playerUuid) {
        cancelWarmup(playerUuid, null);
        teleportCooldowns.remove(playerUuid);
        combatTags.remove(playerUuid);
    }

    /**
     * Teleports player to partner.
     */
    public void requestTeleportToPartner(Player player) {
        UUID pUuid = player.getUniqueId();

        Player partner = plugin.getMarriageManager().getPartnerPlayer(pUuid);
        if (partner == null || !partner.isOnline()) {
            plugin.getLanguageManager().sendMessage(player, "tp-partner-offline");
            return;
        }

        if (!canTeleport(player)) {
            return;
        }

        Location targetLoc = partner.getLocation();
        if (!LocationUtil.isSafeLocation(targetLoc)) {
            plugin.getLanguageManager().sendMessage(player, "tp-unsafe");
            return;
        }

        startTeleportProcess(player, targetLoc, "tp-warmup", "tp-success");
    }

    /**
     * Teleports player to shared couple home.
     */
    public void requestTeleportToHome(Player player) {
        UUID pUuid = player.getUniqueId();

        SharedHome home = plugin.getMarriageManager().getHome(pUuid);
        if (home == null) {
            plugin.getLanguageManager().sendMessage(player, "home-not-set");
            return;
        }

        Location targetLoc = home.toLocation();
        if (targetLoc == null || !LocationUtil.isSafeLocation(targetLoc)) {
            plugin.getLanguageManager().sendMessage(player, "tp-unsafe");
            return;
        }

        if (!canTeleport(player)) {
            return;
        }

        startTeleportProcess(player, targetLoc, "home-warmup", "home-success");
    }

    private boolean canTeleport(Player player) {
        UUID pUuid = player.getUniqueId();
        boolean bypass = player.hasPermission("claritymarriage.tp.bypass");

        if (!bypass && isCombatTagged(pUuid)) {
            long remaining = getCombatTimeRemaining(pUuid);
            plugin.getLanguageManager().sendMessage(player, "tp-cancelled-combat", "time", String.valueOf(remaining));
            return false;
        }

        if (!bypass) {
            long lastTp = teleportCooldowns.getOrDefault(pUuid, 0L);
            int cdSeconds = plugin.getConfigManager().getTeleportCooldown();
            long elapsed = (System.currentTimeMillis() - lastTp) / 1000L;
            if (elapsed < cdSeconds) {
                long rem = cdSeconds - elapsed;
                plugin.getLanguageManager().sendMessage(player, "tp-cooldown", "time", String.valueOf(rem));
                return false;
            }
        }

        return !isWarmingUp(pUuid);
    }

    private void startTeleportProcess(Player player, Location targetLoc, String warmupLangKey, String successLangKey) {
        UUID pUuid = player.getUniqueId();
        int warmup = player.hasPermission("claritymarriage.tp.bypass") ? 0 : plugin.getConfigManager().getTeleportWarmup();

        if (warmup <= 0) {
            executeTeleport(player, targetLoc, successLangKey);
            return;
        }

        plugin.getLanguageManager().sendMessage(player, warmupLangKey, "time", String.valueOf(warmup));
        if (plugin.getConfigManager().isPlaySounds()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundTeleportStart(), 1.0f, 1.0f);
        }

        initialLocations.put(pUuid, player.getLocation().clone());

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTasks.remove(pUuid);
            initialLocations.remove(pUuid);
            if (player.isOnline()) {
                executeTeleport(player, targetLoc, successLangKey);
            }
        }, warmup * 20L);

        pendingTasks.put(pUuid, task);
    }

    private void executeTeleport(Player player, Location targetLoc, String successLangKey) {
        player.teleportAsync(targetLoc).thenAccept(success -> {
            if (Boolean.TRUE.equals(success)) {
                teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                if (plugin.getConfigManager().isPlaySounds()) {
                    player.playSound(player.getLocation(), plugin.getConfigManager().getSoundTeleportSuccess(), 1.0f, 1.0f);
                }
                plugin.getLanguageManager().sendMessage(player, successLangKey);
            } else {
                plugin.getLanguageManager().sendMessage(player, "tp-unsafe");
            }
        });
    }
}
