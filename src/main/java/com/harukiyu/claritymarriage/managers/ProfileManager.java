package com.harukiyu.claritymarriage.managers;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Gender;
import com.harukiyu.claritymarriage.models.PlayerProfile;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player profiles including gender identity and divorce cooldowns.
 */
public class ProfileManager {

    private final ClarityMarriage plugin;
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

    public ProfileManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public void load() {
        profiles.clear();
        plugin.getStorageManager().loadAllProfiles().thenAccept(loaded -> {
            for (PlayerProfile profile : loaded) {
                profiles.put(profile.getUuid(), profile);
            }
            plugin.getLogger().info(() -> String.format("Successfully loaded %d player profile(s) into memory.", loaded.size()));
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player profiles!", ex);
            return null;
        });
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, PlayerProfile::new);
    }

    public Gender getGender(UUID uuid) {
        return getProfile(uuid).getGender();
    }

    public void setGender(UUID uuid, Gender gender) {
        PlayerProfile profile = getProfile(uuid);
        profile.setGender(gender);
        plugin.getStorageManager().saveProfile(profile);
    }

    public boolean hasGender(UUID uuid) {
        return getGender(uuid) != Gender.NONE;
    }

    public boolean isInDivorceCooldown(UUID uuid) {
        PlayerProfile profile = profiles.get(uuid);
        return profile != null && profile.isInDivorceCooldown();
    }

    public long getRemainingDivorceCooldownSeconds(UUID uuid) {
        PlayerProfile profile = profiles.get(uuid);
        return profile != null ? profile.getRemainingDivorceCooldownSeconds() : 0L;
    }

    public void setDivorceCooldown(UUID uuid, long cooldownSeconds) {
        PlayerProfile profile = getProfile(uuid);
        long until = System.currentTimeMillis() + Math.max(0L, cooldownSeconds * 1000L);
        profile.setDivorceCooldownUntil(until);
        plugin.getStorageManager().saveProfile(profile);
    }

    public void clearDivorceCooldown(UUID uuid) {
        PlayerProfile profile = getProfile(uuid);
        profile.setDivorceCooldownUntil(0L);
        plugin.getStorageManager().saveProfile(profile);
    }

    public Collection<PlayerProfile> getAllProfiles() {
        return profiles.values();
    }

    public void saveAll() {
        plugin.getStorageManager().saveAllProfiles(profiles.values());
    }
}
