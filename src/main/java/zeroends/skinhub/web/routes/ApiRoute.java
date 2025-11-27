package zeroends.skinhub.web.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import spark.Request;
import spark.Response;
import spark.Route;
import zeroends.skinhub.SkinHub;
import zeroends.skinhub.model.SkinData;
import zeroends.skinhub.utils.SkinApplier;
import zeroends.skinhub.utils.SkinFetcher;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApiRoute {

    private final SkinHub plugin;
    private final ObjectMapper mapper;

    public final Route handleUpload;
    public final Route handleFetch;
    public final Route handleApply;
    public final Route handleDelete;

    public ApiRoute(SkinHub plugin) {
        this.plugin = plugin;
        this.mapper = new ObjectMapper();
        
        // TRICK: Gunakan variabel final lokal untuk lambda agar compiler tidak bingung
        final SkinHub p = plugin;

        this.handleUpload = (Request req, Response res) -> {
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            UUID uuid = req.attribute("uuid");
            
            int count = p.getDatabaseManager().getWardrobeCount(uuid);
            int max = p.getConfig().getInt("settings.max-wardrobe-slots", 10);
            
            if (count >= max) {
                return jsonError(res, "Wardrobe is full (Max 10).");
            }

            try {
                Part filePart = req.raw().getPart("file");
                if (filePart == null || filePart.getSize() <= 0) {
                    return jsonError(res, "No file uploaded.");
                }

                if (!filePart.getContentType().equals("image/png")) {
                    return jsonError(res, "Only PNG files are allowed.");
                }

                File tempFile = File.createTempFile("skin-" + uuid, ".png");
                try (InputStream input = filePart.getInputStream()) {
                    Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                SkinData skinData = SkinFetcher.generateSkinFromImage(tempFile).join();
                tempFile.delete();

                p.getDatabaseManager().addSkinToWardrobe(uuid, skinData.getName(), skinData.getValue(), skinData.getSignature());

                return jsonSuccess(res, "Skin uploaded successfully.");

            } catch (Exception e) {
                e.printStackTrace();
                return jsonError(res, "Upload failed: " + e.getMessage());
            }
        };

        this.handleFetch = (Request req, Response res) -> {
            UUID uuid = req.attribute("uuid");
            String username = req.queryParams("username");

            if (username == null || username.isEmpty()) {
                return jsonError(res, "Username is required.");
            }

            int count = p.getDatabaseManager().getWardrobeCount(uuid);
            int max = p.getConfig().getInt("settings.max-wardrobe-slots", 10);
            
            if (count >= max) {
                return jsonError(res, "Wardrobe is full.");
            }

            try {
                SkinData skinData = SkinFetcher.fetchSkinFromUsername(username).join();
                p.getDatabaseManager().addSkinToWardrobe(uuid, username, skinData.getValue(), skinData.getSignature());
                return jsonSuccess(res, "Skin fetched successfully.");
            } catch (Exception e) {
                return jsonError(res, "Failed to fetch skin: " + e.getMessage());
            }
        };

        this.handleApply = (Request req, Response res) -> {
            UUID uuid = req.attribute("uuid");
            int id;
            try {
                id = Integer.parseInt(req.params(":id"));
            } catch (NumberFormatException e) {
                return jsonError(res, "Invalid ID.");
            }

            try {
                boolean owned = p.getDatabaseManager().getWardrobe(uuid).stream().anyMatch(s -> s.getId() == id);
                if (!owned) {
                    return jsonError(res, "Skin not found in your wardrobe.");
                }

                SkinData targetSkin = p.getDatabaseManager().getWardrobe(uuid).stream()
                        .filter(s -> s.getId() == id)
                        .findFirst()
                        .orElse(null);

                if (targetSkin != null) {
                    p.getDatabaseManager().setActiveSkin(uuid, targetSkin.getValue(), targetSkin.getSignature());
                    
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        SkinApplier.applySkin(player, targetSkin);
                    }
                    
                    return jsonSuccess(res, "Skin applied successfully.");
                }
                
                return jsonError(res, "Skin not found.");
            } catch (Exception e) {
                return jsonError(res, "Database error.");
            }
        };

        this.handleDelete = (Request req, Response res) -> {
            UUID uuid = req.attribute("uuid");
            int id;
            try {
                id = Integer.parseInt(req.params(":id"));
            } catch (NumberFormatException e) {
                return jsonError(res, "Invalid ID.");
            }

            try {
                p.getDatabaseManager().deleteSkinFromWardrobe(id, uuid);
                return jsonSuccess(res, "Skin deleted.");
            } catch (Exception e) {
                return jsonError(res, "Delete failed.");
            }
        };
    }

    private String jsonSuccess(Response res, String message) {
        res.type("application/json");
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message);
        try {
            return mapper.writeValueAsString(map);
        } catch (Exception e) { return "{}"; }
    }

    private String jsonError(Response res, String message) {
        res.type("application/json");
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", message);
        try {
            return mapper.writeValueAsString(map);
        } catch (Exception e) { return "{}"; }
    }
}