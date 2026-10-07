package com.harukiyu.claritymarriage.storage;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Storage manager that initializes and coordinates the underlying storage engine.
 */
public class StorageManager {

    private final ClarityMarriage plugin;
    private MarriageStorage activeStorage;

    public StorageManager(ClarityMarriage plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String rawType = plugin.getConfig().getString("storage.type", "SQLITE");
        String type = (rawType != null ? rawType : "SQLITE").toUpperCase(java.util.Locale.ROOT);
        if ("YAML".equals(type)) {
            plugin.getLogger().info("Initializing YAML storage (data.yml)...");
            activeStorage = new YamlStorage(plugin, "data.yml");
        } else {
            plugin.getLogger().info("Initializing SQLite storage (marriages.db)...");
            String rawFileName = plugin.getConfig().getString("storage.sqlite.file", "marriages.db");
            String fileName = rawFileName != null ? rawFileName : "marriages.db";
            activeStorage = new SqliteStorage(plugin, fileName);
        }

        try {
            activeStorage.init();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize storage provider! Falling back to YAML...", e);
            activeStorage = new YamlStorage(plugin, "data.yml");
            try {
                activeStorage.init();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize fallback YAML storage!", ex);
            }
        }
    }

    public void close() {
        if (activeStorage != null) {
            activeStorage.close();
        }
    }

    public CompletableFuture<List<Marriage>> loadAllMarriages() {
        return activeStorage.loadAllMarriages();
    }

    public CompletableFuture<Void> saveMarriage(Marriage marriage) {
        return activeStorage.saveMarriage(marriage);
    }

    public CompletableFuture<Void> deleteMarriage(UUID marriageId) {
        return activeStorage.deleteMarriage(marriageId);
    }

    public CompletableFuture<Void> saveAll(Collection<Marriage> marriages) {
        return activeStorage.saveAll(marriages);
    }
}
