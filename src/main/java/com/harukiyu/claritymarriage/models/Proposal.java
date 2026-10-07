package com.harukiyu.claritymarriage.models;

import java.util.UUID;

/**
 * Represents an active marriage proposal sent from one player to another.
 */
public class Proposal {

    private final UUID sender;
    private final UUID receiver;
    private final long timestamp;

    public Proposal(UUID sender, UUID receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isExpired(long timeoutSeconds) {
        return (System.currentTimeMillis() - timestamp) >= (timeoutSeconds * 1000L);
    }
}
