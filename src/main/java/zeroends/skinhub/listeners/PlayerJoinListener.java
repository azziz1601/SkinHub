package zeroends.skinhub.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import zeroends.skinhub.SkinHub;
import zeroends.skinhub.utils.SkinApplier;

public class PlayerJoinListener implements Listener {

    private final SkinHub plugin;

    public PlayerJoinListener(SkinHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Automatically apply the active skin when player joins
        SkinApplier.refreshSkin(event.getPlayer());
    }
}