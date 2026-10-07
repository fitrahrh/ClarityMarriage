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
    private int divorceCooldownSeconds;
    private boolean requireGender;
    private boolean allowGenderChange;
    private boolean allowSameSex;

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
    private Sound soundBankUpgrade;
    private Sound soundLoveExpGain;
    private Sound soundKiss;

    // Bank & EXP Love settings
    private boolean bankEnabled;
    private double bankDefaultLimit;
    private double bankLimitPerLevel;
    private int bankUpgradeCostBase;
    private int bankUpgradeCostPerLevel;

    private double loveProximityDistance;
    private int lovePlaytimeInterval;
    private int lovePlaytimeExp;
    private boolean loveMobKillEnabled;
    private int loveMobKillExp;
    private int loveBossKillExp;
    private boolean loveMiningEnabled;
    private int loveMiningExp;
    private boolean loveFishingEnabled;
    private int loveFishingExp;
    private boolean loveSleepingEnabled;
    private int loveSleepingExp;
    private boolean loveKissEnabled;
    private int loveKissExp;
    private int loveKissCooldown;

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
        divorceCooldownSeconds = config.getInt("marriage.divorce-cooldown-seconds", 86400);
        requireGender = config.getBoolean("marriage.require-gender", true);
        allowGenderChange = config.getBoolean("marriage.allow-gender-change", true);
        allowSameSex = config.getBoolean("marriage.allow-same-sex", true);

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
        soundBankUpgrade = parseSound(config.getString("effects.sounds.bank-upgrade", "UI_TOAST_CHALLENGE_COMPLETE"), Sound.UI_TOAST_CHALLENGE_COMPLETE);
        soundLoveExpGain = parseSound(config.getString("effects.sounds.love-exp-gain", "ENTITY_EXPERIENCE_ORB_PICKUP"), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        soundKiss = parseSound(config.getString("effects.sounds.kiss", "ENTITY_VILLAGER_YES"), Sound.ENTITY_VILLAGER_YES);

        // Bank settings
        bankEnabled = config.getBoolean("bank.enabled", true);
        bankDefaultLimit = config.getDouble("bank.default-limit", 500000.0);
        bankLimitPerLevel = config.getDouble("bank.limit-per-level", 500000.0);
        bankUpgradeCostBase = config.getInt("bank.upgrade-cost-base", 100);
        bankUpgradeCostPerLevel = config.getInt("bank.upgrade-cost-per-level", 50);

        // EXP Love settings
        loveProximityDistance = config.getDouble("love.proximity-distance", 5.0);
        lovePlaytimeInterval = config.getInt("love.playtime.interval-seconds", 600);
        lovePlaytimeExp = config.getInt("love.playtime.exp", 50);

        loveMobKillEnabled = config.getBoolean("love.mob-kill.enabled", true);
        loveMobKillExp = config.getInt("love.mob-kill.exp", 3);
        loveBossKillExp = config.getInt("love.mob-kill.boss-exp", 50);

        loveMiningEnabled = config.getBoolean("love.mining.enabled", true);
        loveMiningExp = config.getInt("love.mining.exp", 2);

        loveFishingEnabled = config.getBoolean("love.fishing.enabled", true);
        loveFishingExp = config.getInt("love.fishing.exp", 2);

        loveSleepingEnabled = config.getBoolean("love.sleeping.enabled", true);
        loveSleepingExp = config.getInt("love.sleeping.exp", 25);

        loveKissEnabled = config.getBoolean("love.kiss.enabled", true);
        loveKissExp = config.getInt("love.kiss.exp", 10);
        loveKissCooldown = config.getInt("love.kiss.cooldown-seconds", 1800);
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
    public int getDivorceCooldownSeconds() { return divorceCooldownSeconds; }
    public boolean isRequireGender() { return requireGender; }
    public boolean isAllowGenderChange() { return allowGenderChange; }
    public boolean isAllowSameSex() { return allowSameSex; }

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
    public Sound getSoundBankUpgrade() { return soundBankUpgrade; }
    public Sound getSoundLoveExpGain() { return soundLoveExpGain; }
    public Sound getSoundKiss() { return soundKiss; }

    // Bank getters
    public boolean isBankEnabled() { return bankEnabled; }
    public double getBankDefaultLimit() { return bankDefaultLimit; }
    public double getBankLimitPerLevel() { return bankLimitPerLevel; }
    public int getBankUpgradeCostBase() { return bankUpgradeCostBase; }
    public int getBankUpgradeCostPerLevel() { return bankUpgradeCostPerLevel; }

    public long getUpgradeCost(int currentLevel) {
        return (long) bankUpgradeCostBase + ((long) (currentLevel - 1) * bankUpgradeCostPerLevel);
    }

    // EXP Love getters
    public double getLoveProximityDistance() { return loveProximityDistance; }
    public int getLovePlaytimeInterval() { return lovePlaytimeInterval; }
    public int getLovePlaytimeExp() { return lovePlaytimeExp; }
    public boolean isLoveMobKillEnabled() { return loveMobKillEnabled; }
    public int getLoveMobKillExp() { return loveMobKillExp; }
    public int getLoveBossKillExp() { return loveBossKillExp; }
    public boolean isLoveMiningEnabled() { return loveMiningEnabled; }
    public int getLoveMiningExp() { return loveMiningExp; }
    public boolean isLoveFishingEnabled() { return loveFishingEnabled; }
    public int getLoveFishingExp() { return loveFishingExp; }
    public boolean isLoveSleepingEnabled() { return loveSleepingEnabled; }
    public int getLoveSleepingExp() { return loveSleepingExp; }
    public boolean isLoveKissEnabled() { return loveKissEnabled; }
    public int getLoveKissExp() { return loveKissExp; }
    public int getLoveKissCooldown() { return loveKissCooldown; }
}
