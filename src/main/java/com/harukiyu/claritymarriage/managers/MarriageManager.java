package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import com.harukiyu.claritymarriage.models.SharedHome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Core manager responsible for caching, querying, and updating marriages.
 */
public class MarriageManager {

    private final ClarityMarriage plugin;
    private final Map<UUID, Marriage> marriagesById = new ConcurrentHashMap<>();
    private final Map<UUID, Marriage> marriagesByPlayer = new ConcurrentHashMap<>();

    public MarriageManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public void load() {
        marriagesById.clear();
        marriagesByPlayer.clear();

        plugin.getStorageManager().loadAllMarriages().thenAccept(marriages -> {
            for (Marriage m : marriages) {
                marriagesById.put(m.getId(), m);
                marriagesByPlayer.put(m.getPlayer1(), m);
                marriagesByPlayer.put(m.getPlayer2(), m);
            }
            plugin.getLogger().info(() -> String.format("Successfully loaded %d marriage(s) into memory.", marriages.size()));
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to load marriages!", ex);
            return null;
        });
    }

    public boolean isMarried(UUID playerUuid) {
        return marriagesByPlayer.containsKey(playerUuid);
    }

    public Marriage getMarriage(UUID playerUuid) {
        return marriagesByPlayer.get(playerUuid);
    }

    public UUID getPartnerUuid(UUID playerUuid) {
        Marriage marriage = getMarriage(playerUuid);
        return marriage != null ? marriage.getPartner(playerUuid) : null;
    }

    public OfflinePlayer getPartnerOfflinePlayer(UUID playerUuid) {
        UUID partnerUuid = getPartnerUuid(playerUuid);
        return partnerUuid != null ? Bukkit.getOfflinePlayer(partnerUuid) : null;
    }

    public Player getPartnerPlayer(UUID playerUuid) {
        UUID partnerUuid = getPartnerUuid(playerUuid);
        return partnerUuid != null ? Bukkit.getPlayer(partnerUuid) : null;
    }

    /**
     * Creates a new marriage between two players.
     */
    public Marriage createMarriage(UUID p1, UUID p2) {
        Marriage marriage = new Marriage(p1, p2);
        marriagesById.put(marriage.getId(), marriage);
        marriagesByPlayer.put(p1, marriage);
        marriagesByPlayer.put(p2, marriage);

        // Async persistence
        plugin.getStorageManager().saveMarriage(marriage);

        Player player1 = Bukkit.getPlayer(p1);
        Player player2 = Bukkit.getPlayer(p2);

        String name1 = player1 != null ? player1.getName() : Bukkit.getOfflinePlayer(p1).getName();
        String name2 = player2 != null ? player2.getName() : Bukkit.getOfflinePlayer(p2).getName();

        // Celebration effects
        if (player1 != null) {
            playMarriageEffects(player1);
            plugin.getLanguageManager().sendMessage(player1, "married-success", "partner", name2 != null ? name2 : "Partner");
        }
        if (player2 != null) {
            playMarriageEffects(player2);
            plugin.getLanguageManager().sendMessage(player2, "married-success", "partner", name1 != null ? name1 : "Partner");
        }

        // Global broadcast
        if (plugin.getConfigManager().isBroadcastOnMarriage()) {
            plugin.getLanguageManager().broadcast("broadcast-marriage",
                    "player1", name1 != null ? name1 : "Someone",
                    "player2", name2 != null ? name2 : "Someone"
            );
        }

        return marriage;
    }

    /**
     * Divorces the couple associated with the given player UUID.
     */
    public boolean divorce(UUID playerUuid) {
        Marriage marriage = marriagesByPlayer.remove(playerUuid);
        if (marriage == null) {
            return false;
        }

        UUID partnerUuid = marriage.getPartner(playerUuid);
        marriagesByPlayer.remove(partnerUuid);
        marriagesById.remove(marriage.getId());

        // Async removal
        plugin.getStorageManager().deleteMarriage(marriage.getId());

        // Disable couple chat if enabled
        plugin.getChatManager().disableChat(playerUuid);
        plugin.getChatManager().disableChat(partnerUuid);

        // Apply divorce cooldown
        int cooldownSeconds = plugin.getConfigManager().getDivorceCooldownSeconds();
        if (cooldownSeconds > 0 && plugin.getProfileManager() != null) {
            plugin.getProfileManager().setDivorceCooldown(playerUuid, cooldownSeconds);
            plugin.getProfileManager().setDivorceCooldown(partnerUuid, cooldownSeconds);
        }

        Player player = Bukkit.getPlayer(playerUuid);
        Player partner = Bukkit.getPlayer(partnerUuid);

        String playerName = player != null ? player.getName() : Bukkit.getOfflinePlayer(playerUuid).getName();
        String partnerName = partner != null ? partner.getName() : Bukkit.getOfflinePlayer(partnerUuid).getName();

        if (player != null) {
            if (plugin.getConfigManager().isPlaySounds()) {
                player.playSound(player.getLocation(), plugin.getConfigManager().getSoundDivorce(), 1.0f, 1.0f);
            }
            plugin.getLanguageManager().sendMessage(player, "divorced-self", "partner", partnerName != null ? partnerName : "Partner");
        }

        if (partner != null) {
            if (plugin.getConfigManager().isPlaySounds()) {
                partner.playSound(partner.getLocation(), plugin.getConfigManager().getSoundDivorce(), 1.0f, 1.0f);
            }
            plugin.getLanguageManager().sendMessage(partner, "divorced-partner", "player", playerName != null ? playerName : "Partner");
        }

        // Broadcast divorce
        if (plugin.getConfigManager().isBroadcastOnDivorce()) {
            plugin.getLanguageManager().broadcast("broadcast-divorce",
                    "player1", playerName != null ? playerName : "Someone",
                    "player2", partnerName != null ? partnerName : "Someone"
            );
        }

        return true;
    }

    /**
     * Sets the shared couple home.
     */
    public boolean setHome(UUID playerUuid, Location location) {
        Marriage marriage = getMarriage(playerUuid);
        if (marriage == null) return false;

        SharedHome home = SharedHome.fromLocation(location);
        marriage.setSharedHome(home);
        plugin.getStorageManager().saveMarriage(marriage);
        return true;
    }

    public SharedHome getHome(UUID playerUuid) {
        Marriage marriage = getMarriage(playerUuid);
        return marriage != null ? marriage.getSharedHome() : null;
    }

    public Collection<Marriage> getAllMarriages() {
        return marriagesById.values();
    }

    public void saveAll() {
        plugin.getStorageManager().saveAll(marriagesById.values());
    }

    private void playMarriageEffects(Player player) {
        if (plugin.getConfigManager().isPlaySounds()) {
            player.playSound(player.getLocation(), plugin.getConfigManager().getSoundMarriageSuccess(), 1.0f, 1.0f);
        }
        if (plugin.getConfigManager().isSpawnParticles()) {
            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 20, 0.5, 0.5, 0.5, 0.1);
        }
    }
}
