package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

/**
 * Manages configuration values from config.yml with type-safe accessors.
 */
public class ConfigManager {

    private final ClarityMarriage plugin;

    private int proposalTimeout;
    private int proposalCooldown;
    private boolean broadcastOnMarriage;
    private boolean broadcastOnDivorce;
    private boolean allowPvp;
    private boolean partnerJoinNotify;

    private int teleportWarmup;
    private int teleportCooldown;
    private boolean teleportCancelOnMove;
    private boolean teleportCancelOnDamage;
    private int combatTagSeconds;

    private String chatFormat;
    private boolean chatPlaySound;
    private Sound chatSound;

    private boolean playSounds;
    private boolean spawnParticles;
    private Sound soundMarriageSuccess;
    private Sound soundProposalSent;
    private Sound soundProposalReceived;
    private Sound soundProposalDenied;
    private Sound soundTeleportStart;
    private Sound soundTeleportSuccess;
    private Sound soundGiftSuccess;
    private Sound soundDivorce;

    public ConfigManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        proposalTimeout = config.getInt("marriage.proposal-timeout", 60);
        proposalCooldown = config.getInt("marriage.proposal-cooldown", 15);
        broadcastOnMarriage = config.getBoolean("marriage.broadcast-on-marriage", true);
        broadcastOnDivorce = config.getBoolean("marriage.broadcast-on-divorce", false);
        allowPvp = config.getBoolean("marriage.allow-pvp", false);
        partnerJoinNotify = config.getBoolean("marriage.partner-join-notify", true);

        teleportWarmup = config.getInt("teleport.warmup-seconds", 3);
        teleportCooldown = config.getInt("teleport.cooldown-seconds", 30);
        teleportCancelOnMove = config.getBoolean("teleport.cancel-on-move", true);
        teleportCancelOnDamage = config.getBoolean("teleport.cancel-on-damage", true);
        combatTagSeconds = config.getInt("teleport.combat-tag-seconds", 10);

        chatFormat = config.getString("chat.format", "<gradient:#ff758c:#ff7eb3>❤ [Couple] <sender>:</gradient> <white><message></white>");
        chatPlaySound = config.getBoolean("chat.play-sound", true);
        chatSound = parseSound(config.getString("chat.sound", "BLOCK_NOTE_BLOCK_CHIME"), Sound.BLOCK_NOTE_BLOCK_CHIME);

        playSounds = config.getBoolean("effects.play-sounds", true);
        spawnParticles = config.getBoolean("effects.spawn-particles", true);

        soundMarriageSuccess = parseSound(config.getString("effects.sounds.marriage-success", "UI_TOAST_CHALLENGE_COMPLETE"), Sound.UI_TOAST_CHALLENGE_COMPLETE);
        soundProposalSent = parseSound(config.getString("effects.sounds.proposal-sent", "BLOCK_NOTE_BLOCK_BELL"), Sound.BLOCK_NOTE_BLOCK_BELL);
        soundProposalReceived = parseSound(config.getString("effects.sounds.proposal-received", "BLOCK_NOTE_BLOCK_BELL"), Sound.BLOCK_NOTE_BLOCK_BELL);
        soundProposalDenied = parseSound(config.getString("effects.sounds.proposal-denied", "ENTITY_VILLAGER_NO"), Sound.ENTITY_VILLAGER_NO);
        soundTeleportStart = parseSound(config.getString("effects.sounds.teleport-start", "BLOCK_PORTAL_TRIGGER"), Sound.BLOCK_PORTAL_TRIGGER);
        soundTeleportSuccess = parseSound(config.getString("effects.sounds.teleport-success", "ENTITY_ENDERMAN_TELEPORT"), Sound.ENTITY_ENDERMAN_TELEPORT);
        soundGiftSuccess = parseSound(config.getString("effects.sounds.gift-success", "ENTITY_PLAYER_LEVELUP"), Sound.ENTITY_PLAYER_LEVELUP);
        soundDivorce = parseSound(config.getString("effects.sounds.divorce", "BLOCK_BEACON_DEACTIVATE"), Sound.BLOCK_BEACON_DEACTIVATE);
    }

    @SuppressWarnings("deprecation")
    private Sound parseSound(String name, Sound fallback) {
        if (name == null || name.isEmpty()) return fallback;
        try {
            return Sound.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Invalid sound specified in config: {0}. Falling back to {1}", new Object[]{name, fallback});
            return fallback;
        }
    }

    public int getProposalTimeout() { return proposalTimeout; }
    public int getProposalCooldown() { return proposalCooldown; }
    public boolean isBroadcastOnMarriage() { return broadcastOnMarriage; }
    public boolean isBroadcastOnDivorce() { return broadcastOnDivorce; }
    public boolean isAllowPvp() { return allowPvp; }
    public boolean isPartnerJoinNotify() { return partnerJoinNotify; }

    public int getTeleportWarmup() { return teleportWarmup; }
    public int getTeleportCooldown() { return teleportCooldown; }
    public boolean isTeleportCancelOnMove() { return teleportCancelOnMove; }
    public boolean isTeleportCancelOnDamage() { return teleportCancelOnDamage; }
    public int getCombatTagSeconds() { return combatTagSeconds; }

    public String getChatFormat() { return chatFormat; }
    public boolean isChatPlaySound() { return chatPlaySound; }
    public Sound getChatSound() { return chatSound; }

    public boolean isPlaySounds() { return playSounds; }
    public boolean isSpawnParticles() { return spawnParticles; }
    public Sound getSoundMarriageSuccess() { return soundMarriageSuccess; }
    public Sound getSoundProposalSent() { return soundProposalSent; }
    public Sound getSoundProposalReceived() { return soundProposalReceived; }
    public Sound getSoundProposalDenied() { return soundProposalDenied; }
    public Sound getSoundTeleportStart() { return soundTeleportStart; }
    public Sound getSoundTeleportSuccess() { return soundTeleportSuccess; }
    public Sound getSoundGiftSuccess() { return soundGiftSuccess; }
    public Sound getSoundDivorce() { return soundDivorce; }
}
