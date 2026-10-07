package com.harukiyu.claritymarriage.storage;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Marriage;
import com.harukiyu.claritymarriage.models.PlayerProfile;
import com.harukiyu.claritymarriage.models.SharedHome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * YAML file implementation of MarriageStorage using data.yml.
 */
public class YamlStorage implements MarriageStorage {

    private final ClarityMarriage plugin;
    private final File dataFile;
    private YamlConfiguration config;
    private final ExecutorService executor;

    public YamlStorage(ClarityMarriage plugin, String fileName) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), fileName);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ClarityMarriage-YAML");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void init() throws Exception {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create data.yml", e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    @Override
    public CompletableFuture<List<Marriage>> loadAllMarriages() {
        return CompletableFuture.supplyAsync(() -> {
            List<Marriage> list = new ArrayList<>();
            this.config = YamlConfiguration.loadConfiguration(dataFile);
            ConfigurationSection section = config.getConfigurationSection("marriages");
            if (section == null) {
                return list;
            }

            for (String key : section.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    String p1Str = section.getString(key + ".player1");
                    String p2Str = section.getString(key + ".player2");
                    if (p1Str == null || p2Str == null) continue;

                    UUID p1 = UUID.fromString(p1Str);
                    UUID p2 = UUID.fromString(p2Str);
                    long date = section.getLong(key + ".marriage-date", System.currentTimeMillis());
                    long playtime = section.getLong(key + ".playtime-seconds", 0L);
                    boolean pvp = section.getBoolean(key + ".pvp-enabled", false);

                    SharedHome home = null;
                    if (section.contains(key + ".home.world")) {
                        String world = section.getString(key + ".home.world");
                        double x = section.getDouble(key + ".home.x");
                        double y = section.getDouble(key + ".home.y");
                        double z = section.getDouble(key + ".home.z");
                        float yaw = (float) section.getDouble(key + ".home.yaw");
                        float pitch = (float) section.getDouble(key + ".home.pitch");
                        home = new SharedHome(world, x, y, z, yaw, pitch);
                    }

                    double bankBalance = section.getDouble(key + ".bank.balance", 0.0);
                    int bankLevel = section.getInt(key + ".bank.level", 1);
                    long loveExp = section.getLong(key + ".love-exp", 0L);

                    list.add(new Marriage(id, p1, p2, date, playtime, home, pvp, bankBalance, bankLevel, loveExp));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, String.format("Failed to parse marriage record for key: %s", key), e);
                }
            }
            return list;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveMarriage(Marriage marriage) {
        return CompletableFuture.runAsync(() -> {
            writeMarriageToConfig(marriage);
            saveToFile();
        }, executor);
    }

    @Override
    public CompletableFuture<Void> deleteMarriage(UUID marriageId) {
        return CompletableFuture.runAsync(() -> {
            config.set("marriages." + marriageId.toString(), null);
            saveToFile();
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveAll(Collection<Marriage> marriages) {
        return CompletableFuture.runAsync(() -> {
            for (Marriage marriage : marriages) {
                writeMarriageToConfig(marriage);
            }
            saveToFile();
        }, executor);
    }

    private void writeMarriageToConfig(Marriage marriage) {
        String path = "marriages." + marriage.getId().toString();
        config.set(path + ".player1", marriage.getPlayer1().toString());
        config.set(path + ".player2", marriage.getPlayer2().toString());
        config.set(path + ".marriage-date", marriage.getMarriageDate());
        config.set(path + ".playtime-seconds", marriage.getPlaytimeSeconds());
        config.set(path + ".pvp-enabled", marriage.isPvpEnabled());
        config.set(path + ".bank.balance", marriage.getBankBalance());
        config.set(path + ".bank.level", marriage.getBankLevel());
        config.set(path + ".love-exp", marriage.getLoveExp());

        SharedHome home = marriage.getSharedHome();
        if (home != null) {
            config.set(path + ".home.world", home.getWorldName());
            config.set(path + ".home.x", home.getX());
            config.set(path + ".home.y", home.getY());
            config.set(path + ".home.z", home.getZ());
            config.set(path + ".home.yaw", home.getYaw());
            config.set(path + ".home.pitch", home.getPitch());
        } else {
            config.set(path + ".home", null);
        }
    }

    @Override
    public CompletableFuture<List<PlayerProfile>> loadAllProfiles() {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerProfile> list = new ArrayList<>();
            this.config = YamlConfiguration.loadConfiguration(dataFile);
            ConfigurationSection section = config.getConfigurationSection("profiles");
            if (section == null) return list;

            for (String key : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String genderStr = section.getString(key + ".gender", "NONE");
                    com.harukiyu.claritymarriage.models.Gender gender = com.harukiyu.claritymarriage.models.Gender.fromString(genderStr);
                    long cooldown = section.getLong(key + ".divorce-cooldown-until", 0L);
                    list.add(new PlayerProfile(uuid, gender, cooldown));
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, String.format("Failed to parse profile record for key: %s", key), e);
                }
            }
            return list;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveProfile(PlayerProfile profile) {
        return CompletableFuture.runAsync(() -> {
            String path = "profiles." + profile.getUuid().toString();
            config.set(path + ".gender", profile.getGender().name());
            config.set(path + ".divorce-cooldown-until", profile.getDivorceCooldownUntil());
            saveToFile();
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveAllProfiles(Collection<PlayerProfile> profiles) {
        return CompletableFuture.runAsync(() -> {
            for (PlayerProfile profile : profiles) {
                String path = "profiles." + profile.getUuid().toString();
                config.set(path + ".gender", profile.getGender().name());
                config.set(path + ".divorce-cooldown-until", profile.getDivorceCooldownUntil());
            }
            saveToFile();
        }, executor);
    }

    private synchronized void saveToFile() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save data.yml", e);
        }
    }
}
