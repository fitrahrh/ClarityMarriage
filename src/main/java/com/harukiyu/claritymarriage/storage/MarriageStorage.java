package com.harukiyu.claritymarriage.storage;

import com.harukiyu.claritymarriage.models.Marriage;
import com.harukiyu.claritymarriage.models.PlayerProfile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Storage interface for persisting and loading marriage records and player profiles.
 */
public interface MarriageStorage {

    /**
     * Initializes the storage mechanism (creates tables, files, connections).
     */
    void init() throws Exception;

    /**
     * Closes the storage mechanism and releases resources.
     */
    void close();

    /**
     * Asynchronously loads all marriages into memory.
     */
    CompletableFuture<List<Marriage>> loadAllMarriages();

    /**
     * Asynchronously saves or updates a single marriage record.
     */
    CompletableFuture<Void> saveMarriage(Marriage marriage);

    /**
     * Asynchronously deletes a marriage record by ID.
     */
    CompletableFuture<Void> deleteMarriage(UUID marriageId);

    /**
     * Asynchronously saves all provided marriages.
     */
    CompletableFuture<Void> saveAll(Collection<Marriage> marriages);

    /**
     * Asynchronously loads all player profiles (gender, divorce cooldown).
     */
    CompletableFuture<List<PlayerProfile>> loadAllProfiles();

    /**
     * Asynchronously saves or updates a player profile.
     */
    CompletableFuture<Void> saveProfile(PlayerProfile profile);

    /**
     * Asynchronously saves all player profiles.
     */
    CompletableFuture<Void> saveAllProfiles(Collection<PlayerProfile> profiles);
}
