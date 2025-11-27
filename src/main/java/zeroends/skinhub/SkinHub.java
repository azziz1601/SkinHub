package zeroends.skinhub;

import org.bukkit.plugin.java.JavaPlugin;
import zeroends.skinhub.commands.SkinHubCommand;
import zeroends.skinhub.database.DatabaseManager;
import zeroends.skinhub.listeners.PlayerJoinListener;
import zeroends.skinhub.web.WebServer;

import java.sql.SQLException;
import java.util.logging.Level;

public final class SkinHub extends JavaPlugin {

    private static SkinHub instance;
    private DatabaseManager databaseManager;
    private WebServer webServer;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        try {
            this.databaseManager = new DatabaseManager(this);
            this.databaseManager.initialize();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database! Disabling plugin...", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.webServer = new WebServer(this);
        this.webServer.start();

        getCommand("skinhub").setExecutor(new SkinHubCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        getLogger().info("SkinHub has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (webServer != null) {
            webServer.stop();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("SkinHub has been disabled.");
    }

    public static SkinHub getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public WebServer getWebServer() {
        return webServer;
    }
}