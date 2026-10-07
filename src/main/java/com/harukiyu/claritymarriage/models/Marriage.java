package com.harukiyu.claritymarriage.models;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a marriage bond between two players, including shared bank and EXP Love.
 */
public class Marriage {

    private final UUID id;
    private final UUID player1;
    private final UUID player2;
    private final long marriageDate;
    private final AtomicLong playtimeSeconds;
    private volatile SharedHome sharedHome;
    private volatile boolean pvpEnabled;

    // Bank & EXP Love fields
    private volatile double bankBalance;
    private final AtomicInteger bankLevel;
    private final AtomicLong loveExp;

    public Marriage(UUID id, UUID player1, UUID player2, long marriageDate, long playtimeSeconds,
                    SharedHome sharedHome, boolean pvpEnabled, double bankBalance, int bankLevel, long loveExp) {
        this.id = id != null ? id : UUID.randomUUID();
        this.player1 = player1;
        this.player2 = player2;
        this.marriageDate = marriageDate;
        this.playtimeSeconds = new AtomicLong(playtimeSeconds);
        this.sharedHome = sharedHome;
        this.pvpEnabled = pvpEnabled;
        this.bankBalance = Math.max(0.0, bankBalance);
        this.bankLevel = new AtomicInteger(Math.max(1, bankLevel));
        this.loveExp = new AtomicLong(Math.max(0L, loveExp));
    }

    public Marriage(UUID id, UUID player1, UUID player2, long marriageDate, long playtimeSeconds, SharedHome sharedHome, boolean pvpEnabled) {
        this(id, player1, player2, marriageDate, playtimeSeconds, sharedHome, pvpEnabled, 0.0, 1, 0L);
    }

    public Marriage(UUID player1, UUID player2) {
        this(UUID.randomUUID(), player1, player2, System.currentTimeMillis(), 0L, null, false, 0.0, 1, 0L);
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

    // Bank balance operations
    public synchronized double getBankBalance() {
        return bankBalance;
    }

    public synchronized void setBankBalance(double bankBalance) {
        this.bankBalance = Math.max(0.0, bankBalance);
    }

    public synchronized boolean addBankBalance(double amount, double maxLimit) {
        if (amount <= 0) return false;
        if (this.bankBalance + amount > maxLimit) {
            return false;
        }
        this.bankBalance += amount;
        return true;
    }

    public synchronized boolean withdrawBankBalance(double amount) {
        if (amount <= 0 || this.bankBalance < amount) {
            return false;
        }
        this.bankBalance -= amount;
        return true;
    }

    // Bank level & limit operations
    public int getBankLevel() {
        return bankLevel.get();
    }

    public void setBankLevel(int level) {
        this.bankLevel.set(Math.max(1, level));
    }

    public int incrementBankLevel() {
        return this.bankLevel.incrementAndGet();
    }

    public double getBankLimit(double baseLimit, double limitPerLevel) {
        return baseLimit + ((double) (bankLevel.get() - 1) * limitPerLevel);
    }

    // EXP Love operations
    public long getLoveExp() {
        return loveExp.get();
    }

    public void setLoveExp(long exp) {
        this.loveExp.set(Math.max(0L, exp));
    }

    public void addLoveExp(long amount) {
        if (amount > 0) {
            this.loveExp.addAndGet(amount);
        }
    }

    public boolean spendLoveExp(long amount) {
        if (amount <= 0) return false;
        while (true) {
            long current = this.loveExp.get();
            if (current < amount) {
                return false;
            }
            if (this.loveExp.compareAndSet(current, current - amount)) {
                return true;
            }
        }
    }

    /**
     * Checks if the given UUID is part of this marriage.
     */
    public boolean contains(UUID uuid) {
        return player1.equals(uuid) || player2.equals(uuid);
    }

    /**
     * Retrieves the partner's UUID of the given player.
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
