package zeroends.skinhub.web.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;
import spark.Response;
import spark.Route;
import zeroends.skinhub.SkinHub;
import zeroends.skinhub.model.SkinData;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class DashboardRoute {

    private final SkinHub plugin;
    private final ObjectMapper mapper;
    public final Route serveDashboard;

    public DashboardRoute(SkinHub plugin) {
        this.plugin = plugin;
        this.mapper = new ObjectMapper();

        // Menggunakan parameter 'plugin' di dalam lambda
        this.serveDashboard = (Request req, Response res) -> {
            String sessionId = req.cookie("skinhub_session");
            if (sessionId == null) {
                res.redirect("/login");
                return null;
            }

            UUID uuid = plugin.getDatabaseManager().validateSession(sessionId);
            if (uuid == null) {
                res.removeCookie("skinhub_session");
                res.redirect("/login");
                return null;
            }

            List<SkinData> wardrobe = plugin.getDatabaseManager().getWardrobe(uuid);
            SkinData activeSkin = plugin.getDatabaseManager().getActiveSkin(uuid);
            int maxSkins = plugin.getConfig().getInt("settings.max-wardrobe-slots", 10);

            String wardrobeJson = mapper.writeValueAsString(wardrobe);
            String activeSkinJson = activeSkin != null ? mapper.writeValueAsString(activeSkin) : "null";

            String html = loadTemplate("dashboard.html");
            
            html = html.replace("{{wardrobe_json}}", wardrobeJson);
            html = html.replace("{{active_skin_json}}", activeSkinJson);
            html = html.replace("{{max_skins}}", String.valueOf(maxSkins));
            html = html.replace("{{current_count}}", String.valueOf(wardrobe.size()));

            return html;
        };
    }

    private String loadTemplate(String name) {
        try (InputStream is = getClass().getResourceAsStream("/web/templates/" + name)) {
            if (is == null) return "";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}