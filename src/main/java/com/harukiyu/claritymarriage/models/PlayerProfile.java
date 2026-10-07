package com.harukiyu.claritymarriage.models;

import java.util.UUID;

/**
 * Stores individual player metadata including gender and divorce cooldown.
 */
public class PlayerProfile {

    private final UUID uuid;
    private volatile Gender gender;
    private volatile long divorceCooldownUntil;

    public PlayerProfile(UUID uuid, Gender gender, long divorceCooldownUntil) {
        this.uuid = uuid;
        this.gender = gender != null ? gender : Gender.NONE;
        this.divorceCooldownUntil = divorceCooldownUntil;
    }

    public PlayerProfile(UUID uuid) {
        this(uuid, Gender.NONE, 0L);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender != null ? gender : Gender.NONE;
    }

    public long getDivorceCooldownUntil() {
        return divorceCooldownUntil;
    }

    public void setDivorceCooldownUntil(long divorceCooldownUntil) {
        this.divorceCooldownUntil = divorceCooldownUntil;
    }

    public boolean isInDivorceCooldown() {
        return System.currentTimeMillis() < divorceCooldownUntil;
    }

    public long getRemainingDivorceCooldownSeconds() {
        long remainingMs = divorceCooldownUntil - System.currentTimeMillis();
        return Math.max(0L, (remainingMs + 999L) / 1000L);
    }
}
