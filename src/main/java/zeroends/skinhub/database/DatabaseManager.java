package zeroends.skinhub.database;

import zeroends.skinhub.SkinHub;
import zeroends.skinhub.model.SkinData;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final SkinHub plugin;
    private Connection connection;

    public DatabaseManager(SkinHub plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String dbFileName = plugin.getConfig().getString("database.file", "skinhub.db");
        String url = "jdbc:sqlite:" + new File(dataFolder, dbFileName).getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            createTables();
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    private synchronized void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS skinhub_tokens (" +
                    "token TEXT PRIMARY KEY, " +
                    "uuid TEXT NOT NULL, " +
                    "player_name TEXT NOT NULL, " +
                    "expiry BIGINT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS skinhub_sessions (" +
                    "session_id TEXT PRIMARY KEY, " +
                    "uuid TEXT NOT NULL, " +
                    "expiry BIGINT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS skinhub_wardrobe (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uuid TEXT NOT NULL, " +
                    "skin_name TEXT NOT NULL, " +
                    "value TEXT NOT NULL, " +
                    "signature TEXT, " +
                    "timestamp BIGINT NOT NULL)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS skinhub_active_skins (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "value TEXT NOT NULL, " +
                    "signature TEXT)");
        }
    }

    public synchronized void saveToken(String token, UUID uuid, String playerName, long expiry) throws SQLException {
        String sql = "INSERT OR REPLACE INTO skinhub_tokens (token, uuid, player_name, expiry) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.setString(2, uuid.toString());
            pstmt.setString(3, playerName);
            pstmt.setLong(4, expiry);
            pstmt.executeUpdate();
        }
        plugin.getLogger().info("DEBUG: Token saved for " + playerName + " [" + token + "]");
    }

    public synchronized String validateToken(String token) throws SQLException {
        String sql = "SELECT uuid, expiry FROM skinhub_tokens WHERE token = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                long expiry = rs.getLong("expiry");
                long now = System.currentTimeMillis();
                
                if (now < expiry) {
                    String uuid = rs.getString("uuid");
                    deleteToken(token);
                    plugin.getLogger().info("DEBUG: Token VALID for UUID " + uuid);
                    return uuid;
                } else {
                    plugin.getLogger().warning("DEBUG: Token EXPIRED. Now: " + now + ", Expiry: " + expiry);
                    deleteToken(token); 
                }
            } else {
                plugin.getLogger().warning("DEBUG: Token NOT FOUND in DB: " + token);
            }
        }
        return null;
    }

    private synchronized void deleteToken(String token) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM skinhub_tokens WHERE token = ?")) {
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        }
    }

    public synchronized void createSession(String sessionId, UUID uuid, long expiry) throws SQLException {
        String sql = "INSERT INTO skinhub_sessions (session_id, uuid, expiry) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.setString(2, uuid.toString());
            pstmt.setLong(3, expiry);
            pstmt.executeUpdate();
        }
    }

    public synchronized UUID validateSession(String sessionId) throws SQLException {
        String sql = "SELECT uuid, expiry FROM skinhub_sessions WHERE session_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                if (System.currentTimeMillis() < rs.getLong("expiry")) {
                    return UUID.fromString(rs.getString("uuid"));
                } else {
                    deleteSession(sessionId);
                }
            }
        }
        return null;
    }

    public synchronized void deleteSession(String sessionId) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM skinhub_sessions WHERE session_id = ?")) {
            pstmt.setString(1, sessionId);
            pstmt.executeUpdate();
        }
    }

    public synchronized void addSkinToWardrobe(UUID uuid, String name, String value, String signature) throws SQLException {
        String sql = "INSERT INTO skinhub_wardrobe (uuid, skin_name, value, signature, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setString(3, value);
            pstmt.setString(4, signature);
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        }
    }

    public synchronized void deleteSkinFromWardrobe(int id, UUID uuid) throws SQLException {
        String sql = "DELETE FROM skinhub_wardrobe WHERE id = ? AND uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        }
    }

    public synchronized List<SkinData> getWardrobe(UUID uuid) throws SQLException {
        List<SkinData> skins = new ArrayList<>();
        String sql = "SELECT id, skin_name, value, signature FROM skinhub_wardrobe WHERE uuid = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                skins.add(new SkinData(
                    rs.getInt("id"),
                    rs.getString("skin_name"),
                    rs.getString("value"),
                    rs.getString("signature")
                ));
            }
        }
        return skins;
    }

    public synchronized int getWardrobeCount(UUID uuid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM skinhub_wardrobe WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public synchronized void setActiveSkin(UUID uuid, String value, String signature) throws SQLException {
        String sql = "INSERT OR REPLACE INTO skinhub_active_skins (uuid, value, signature) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, value);
            pstmt.setString(3, signature);
            pstmt.executeUpdate();
        }
    }

    public synchronized SkinData getActiveSkin(UUID uuid) throws SQLException {
        String sql = "SELECT value, signature FROM skinhub_active_skins WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new SkinData(0, "Current", rs.getString("value"), rs.getString("signature"));
            }
        }
        return null;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}