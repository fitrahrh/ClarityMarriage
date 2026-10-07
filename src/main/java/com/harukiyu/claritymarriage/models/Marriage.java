package com.harukiyu.claritymarriage.models;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a marriage bond between two players.
 */
public class Marriage {

    private final UUID id;
    private final UUID player1;
    private final UUID player2;
    private final long marriageDate;
    private final AtomicLong playtimeSeconds;
    private volatile SharedHome sharedHome;
    private volatile boolean pvpEnabled;

    public Marriage(UUID id, UUID player1, UUID player2, long marriageDate, long playtimeSeconds, SharedHome sharedHome, boolean pvpEnabled) {
        this.id = id != null ? id : UUID.randomUUID();
        this.player1 = player1;
        this.player2 = player2;
        this.marriageDate = marriageDate;
        this.playtimeSeconds = new AtomicLong(playtimeSeconds);
        this.sharedHome = sharedHome;
        this.pvpEnabled = pvpEnabled;
    }

    public Marriage(UUID player1, UUID player2) {
        this(UUID.randomUUID(), player1, player2, System.currentTimeMillis(), 0L, null, false);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public long getMarriageDate() {
        return marriageDate;
    }

    public long getPlaytimeSeconds() {
        return playtimeSeconds.get();
    }

    public void addPlaytime(long seconds) {
        this.playtimeSeconds.addAndGet(seconds);
    }

    public void setPlaytimeSeconds(long seconds) {
        this.playtimeSeconds.set(seconds);
    }

    public SharedHome getSharedHome() {
        return sharedHome;
    }

    public void setSharedHome(SharedHome sharedHome) {
        this.sharedHome = sharedHome;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    /**
     * Checks if the given UUID is part of this marriage.
     */
    public boolean contains(UUID uuid) {
        return player1.equals(uuid) || player2.equals(uuid);
    }

    /**
     * Retrieves the partner's UUID of the given player.
     * Returns null if the given UUID is not part of this marriage.
     */
    public UUID getPartner(UUID uuid) {
        if (player1.equals(uuid)) {
            return player2;
        } else if (player2.equals(uuid)) {
            return player1;
        }
        return null;
    }
}
