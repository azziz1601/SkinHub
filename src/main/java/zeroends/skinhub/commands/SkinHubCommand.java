package zeroends.skinhub.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import zeroends.skinhub.SkinHub;
import zeroends.skinhub.utils.TokenGenerator;

import java.sql.SQLException;
import java.util.logging.Level;

public class SkinHubCommand implements CommandExecutor {

    private final SkinHub plugin;
    private final MiniMessage mm;

    public SkinHubCommand(SkinHub plugin) {
        this.plugin = plugin;
        this.mm = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.prefix") + "<red>Usage: /skinhub <link|reload>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("skinhub.admin")) {
                sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.no-permission")));
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.reload-success")));
            return true;
        }

        if (args[0].equalsIgnoreCase("link")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.only-players")));
                return true;
            }

            if (!player.hasPermission("skinhub.use")) {
                player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.no-permission")));
                return true;
            }

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    String token = TokenGenerator.generate();
                    long expirySeconds = plugin.getConfig().getLong("settings.token-expiry-seconds", 180);
                    long expiryTime = System.currentTimeMillis() + (expirySeconds * 1000);

                    plugin.getDatabaseManager().saveToken(token, player.getUniqueId(), player.getName(), expiryTime);

                    String baseUrl = plugin.getConfig().getString("web-server.public-url", "http://your-ip:25566");
                    // Pastikan tidak ada slash di akhir config
                    if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    
                    String fullUrl = baseUrl + "/login?token=" + token;
                    
                    // Format pesan baru: Simple & Direct
                    String msgFormat = "<gray>Manage your skin here: <click:open_url:'" + fullUrl + "'><aqua><u>CLICK TO OPEN SKINHUB</u></aqua></click>";
                    
                    player.sendMessage(mm.deserialize(msgFormat));

                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error generating token", e);
                    player.sendMessage(mm.deserialize("<red>Internal error generating token."));
                }
            });
            return true;
        }

        sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.prefix") + "<red>Unknown subcommand."));
        return true;
    }
}