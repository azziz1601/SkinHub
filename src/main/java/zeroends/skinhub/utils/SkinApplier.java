package zeroends.skinhub.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import zeroends.skinhub.SkinHub;
import zeroends.skinhub.model.SkinData;

import java.util.UUID;

public class SkinApplier {

    public static void applySkin(Player player, SkinData skinData) {
        if (player == null || !player.isOnline() || skinData == null) {
            return;
        }

        Bukkit.getScheduler().runTask(SkinHub.getInstance(), () -> {
            PlayerProfile profile = player.getPlayerProfile();
            
            // FIX: Hapus properti 'textures' menggunakan removeIf pada Collection
            profile.getProperties().removeIf(prop -> "textures".equals(prop.getName()));
            
            profile.setProperty(new ProfileProperty("textures", skinData.getValue(), skinData.getSignature()));
            player.setPlayerProfile(profile);
            
            // Hide and show player to refresh skin for others
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.getUniqueId().equals(player.getUniqueId())) continue;
                target.hidePlayer(SkinHub.getInstance(), player);
                target.showPlayer(SkinHub.getInstance(), player);
            }
        });
    }

    public static void refreshSkin(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(SkinHub.getInstance(), () -> {
            try {
                SkinData activeSkin = SkinHub.getInstance().getDatabaseManager().getActiveSkin(player.getUniqueId());
                if (activeSkin != null) {
                    applySkin(player, activeSkin);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}