package com.harukiyu.claritymarriage.storage;

import com.harukiyu.claritymarriage.ClarityMarriage;
import com.harukiyu.claritymarriage.models.Gender;
import com.harukiyu.claritymarriage.models.Marriage;
import com.harukiyu.claritymarriage.models.PlayerProfile;
import com.harukiyu.claritymarriage.models.SharedHome;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * SQLite implementation of MarriageStorage for robust, lag-free asynchronous persistence.
 */
public class SqliteStorage implements MarriageStorage {

    private final ClarityMarriage plugin;
    private final File dbFile;
    private Connection connection;
    private final ExecutorService executor;

    public SqliteStorage(ClarityMarriage plugin, String fileName) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), fileName);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ClarityMarriage-SQLite");
            t.setDaemon(true);
            return t;
        });
    }

    private synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }
        return connection;
    }

    @Override
    public void init() throws Exception {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
            // Native driver already registered in modern runtimes
        }

        try (Statement statement = getConnection().createStatement()) {
            // Marriages table
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cm_marriages (
                    id VARCHAR(36) PRIMARY KEY,
                    player1 VARCHAR(36) NOT NULL,
                    player2 VARCHAR(36) NOT NULL,
                    marriage_date BIGINT NOT NULL,
                    playtime_seconds BIGINT NOT NULL,
                    pvp_enabled INTEGER NOT NULL DEFAULT 0,
                    home_world VARCHAR(64),
                    home_x DOUBLE,
                    home_y DOUBLE,
                    home_z DOUBLE,
                    home_yaw REAL,
                    home_pitch REAL,
                    bank_balance REAL NOT NULL DEFAULT 0.0,
                    bank_level INTEGER NOT NULL DEFAULT 1,
                    love_exp INTEGER NOT NULL DEFAULT 0
                );
            """);

            // Profiles table (gender, divorce cooldown)
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cm_profiles (
                    uuid VARCHAR(36) PRIMARY KEY,
                    gender VARCHAR(16) NOT NULL DEFAULT 'NONE',
                    divorce_cooldown_until BIGINT NOT NULL DEFAULT 0
                );
            """);

            // Migration alter statements for existing marriages table
            try {
                statement.executeUpdate("ALTER TABLE cm_marriages ADD COLUMN bank_balance REAL NOT NULL DEFAULT 0.0;");
            } catch (SQLException ignored) {}
            try {
                statement.executeUpdate("ALTER TABLE cm_marriages ADD COLUMN bank_level INTEGER NOT NULL DEFAULT 1;");
            } catch (SQLException ignored) {}
            try {
                statement.executeUpdate("ALTER TABLE cm_marriages ADD COLUMN love_exp INTEGER NOT NULL DEFAULT 0;");
            } catch (SQLException ignored) {}
        }
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to close SQLite connection cleanly", e);
        }
    }

    @Override
    public CompletableFuture<List<Marriage>> loadAllMarriages() {
        return CompletableFuture.supplyAsync(() -> {
            List<Marriage> list = new ArrayList<>();
            String sql = "SELECT * FROM cm_marriages;";
            try (Statement statement = getConnection().createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {

                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    UUID p1 = UUID.fromString(rs.getString("player1"));
                    UUID p2 = UUID.fromString(rs.getString("player2"));
                    long date = rs.getLong("marriage_date");
                    long playtime = rs.getLong("playtime_seconds");
                    boolean pvp = rs.getInt("pvp_enabled") == 1;

                    SharedHome home = null;
                    String world = rs.getString("home_world");
                    if (world != null && !world.isEmpty()) {
                        home = new SharedHome(
                                world,
                                rs.getDouble("home_x"),
                                rs.getDouble("home_y"),
                                rs.getDouble("home_z"),
                                rs.getFloat("home_yaw"),
                                rs.getFloat("home_pitch")
                        );
                    }

                    double bankBalance = rs.getDouble("bank_balance");
                    int bankLevel = rs.getInt("bank_level");
                    if (bankLevel <= 0) bankLevel = 1;
                    long loveExp = rs.getLong("love_exp");

                    list.add(new Marriage(id, p1, p2, date, playtime, home, pvp, bankBalance, bankLevel, loveExp));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load marriages from SQLite", e);
            }
            return list;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveMarriage(Marriage marriage) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO cm_marriages (
                    id, player1, player2, marriage_date, playtime_seconds, pvp_enabled,
                    home_world, home_x, home_y, home_z, home_yaw, home_pitch,
                    bank_balance, bank_level, love_exp
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    player1 = excluded.player1,
                    player2 = excluded.player2,
                    marriage_date = excluded.marriage_date,
                    playtime_seconds = excluded.playtime_seconds,
                    pvp_enabled = excluded.pvp_enabled,
                    home_world = excluded.home_world,
                    home_x = excluded.home_x,
                    home_y = excluded.home_y,
                    home_z = excluded.home_z,
                    home_yaw = excluded.home_yaw,
                    home_pitch = excluded.home_pitch,
                    bank_balance = excluded.bank_balance,
                    bank_level = excluded.bank_level,
                    love_exp = excluded.love_exp;
            """;

            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                fillStatement(ps, marriage);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, String.format("Failed to save marriage to SQLite: %s", marriage.getId()), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> deleteMarriage(UUID marriageId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM cm_marriages WHERE id = ?;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, marriageId.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, String.format("Failed to delete marriage from SQLite: %s", marriageId), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveAll(Collection<Marriage> marriages) {
        return CompletableFuture.runAsync(() -> {
            if (marriages.isEmpty()) return;
            String sql = """
                INSERT INTO cm_marriages (
                    id, player1, player2, marriage_date, playtime_seconds, pvp_enabled,
                    home_world, home_x, home_y, home_z, home_yaw, home_pitch,
                    bank_balance, bank_level, love_exp
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    player1 = excluded.player1,
                    player2 = excluded.player2,
                    marriage_date = excluded.marriage_date,
                    playtime_seconds = excluded.playtime_seconds,
                    pvp_enabled = excluded.pvp_enabled,
                    home_world = excluded.home_world,
                    home_x = excluded.home_x,
                    home_y = excluded.home_y,
                    home_z = excluded.home_z,
                    home_yaw = excluded.home_yaw,
                    home_pitch = excluded.home_pitch,
                    bank_balance = excluded.bank_balance,
                    bank_level = excluded.bank_level,
                    love_exp = excluded.love_exp;
            """;

            try {
                Connection conn = getConnection();
                boolean prevAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (Marriage marriage : marriages) {
                        fillStatement(ps, marriage);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(prevAutoCommit);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to batch save marriages to SQLite", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<PlayerProfile>> loadAllProfiles() {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerProfile> list = new ArrayList<>();
            String sql = "SELECT * FROM cm_profiles;";
            try (Statement statement = getConnection().createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    Gender gender = Gender.fromString(rs.getString("gender"));
                    long cooldown = rs.getLong("divorce_cooldown_until");
                    list.add(new PlayerProfile(uuid, gender, cooldown));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player profiles from SQLite", e);
            }
            return list;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveProfile(PlayerProfile profile) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO cm_profiles (uuid, gender, divorce_cooldown_until)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    gender = excluded.gender,
                    divorce_cooldown_until = excluded.divorce_cooldown_until;
            """;

            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, profile.getUuid().toString());
                ps.setString(2, profile.getGender().name());
                ps.setLong(3, profile.getDivorceCooldownUntil());
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, String.format("Failed to save profile to SQLite: %s", profile.getUuid()), e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveAllProfiles(Collection<PlayerProfile> profiles) {
        return CompletableFuture.runAsync(() -> {
            if (profiles.isEmpty()) return;
            String sql = """
                INSERT INTO cm_profiles (uuid, gender, divorce_cooldown_until)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    gender = excluded.gender,
                    divorce_cooldown_until = excluded.divorce_cooldown_until;
            """;

            try {
                Connection conn = getConnection();
                boolean prevAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (PlayerProfile profile : profiles) {
                        ps.setString(1, profile.getUuid().toString());
                        ps.setString(2, profile.getGender().name());
                        ps.setLong(3, profile.getDivorceCooldownUntil());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(prevAutoCommit);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to batch save profiles to SQLite", e);
            }
        }, executor);
    }

    private void fillStatement(PreparedStatement ps, Marriage marriage) throws SQLException {
        ps.setString(1, marriage.getId().toString());
        ps.setString(2, marriage.getPlayer1().toString());
        ps.setString(3, marriage.getPlayer2().toString());
        ps.setLong(4, marriage.getMarriageDate());
        ps.setLong(5, marriage.getPlaytimeSeconds());
        ps.setInt(6, marriage.isPvpEnabled() ? 1 : 0);

        SharedHome home = marriage.getSharedHome();
        if (home != null) {
            ps.setString(7, home.getWorldName());
            ps.setDouble(8, home.getX());
            ps.setDouble(9, home.getY());
            ps.setDouble(10, home.getZ());
            ps.setFloat(11, home.getYaw());
            ps.setFloat(12, home.getPitch());
        } else {
            ps.setString(7, null);
            ps.setObject(8, null);
            ps.setObject(9, null);
            ps.setObject(10, null);
            ps.setObject(11, null);
            ps.setObject(12, null);
        }

        ps.setDouble(13, marriage.getBankBalance());
        ps.setInt(14, marriage.getBankLevel());
        ps.setLong(15, marriage.getLoveExp());
    }
}
