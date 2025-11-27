package zeroends.skinhub.web.routes;

import spark.Filter;
import spark.Route;
import zeroends.skinhub.SkinHub;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthRoute {

    private final SkinHub plugin;
    public final Route handleLogin;
    public final Route handleVerify;
    public final Filter requireAuth;

    public AuthRoute(SkinHub plugin) {
        this.plugin = plugin;
        
        final SkinHub p = plugin;

        this.handleLogin = (req, res) -> {
            String token = req.queryParams("token");
            String html = loadTemplate("login.html");
            html = html.replace("{{token}}", token != null ? token : "");
            html = html.replace("{{error_display}}", req.queryParams("error") != null ? "block" : "none");
            return html;
        };

        this.handleVerify = (req, res) -> {
            String rawToken = req.queryParams("token");
            
            if (rawToken == null || rawToken.isEmpty()) {
                res.redirect("/login?error=1");
                return null;
            }

            String token = rawToken.trim().toUpperCase();
            
            p.getLogger().info("Web Auth Attempt: Raw='" + rawToken + "', Processed='" + token + "'");

            String uuidStr = p.getDatabaseManager().validateToken(token);

            if (uuidStr != null) {
                UUID playerUuid = UUID.fromString(uuidStr);
                String sessionId = UUID.randomUUID().toString();
                long sessionDays = p.getConfig().getLong("settings.session-expiry-days", 7);
                long expiry = System.currentTimeMillis() + (sessionDays * 24 * 60 * 60 * 1000);

                p.getDatabaseManager().createSession(sessionId, playerUuid, expiry);

                // FIX: Menambahkan path "/" pada cookie agar valid di seluruh domain
                res.cookie("/", "skinhub_session", sessionId, (int) (sessionDays * 24 * 60 * 60), false, true);
                
                res.redirect("/manage");
            } else {
                res.redirect("/login?error=1");
            }
            return null;
        };

        this.requireAuth = (req, res) -> {
            String sessionId = req.cookie("skinhub_session");
            if (sessionId == null) {
                res.redirect("/login");
                spark.Spark.halt();
            }

            UUID uuid = p.getDatabaseManager().validateSession(sessionId);
            if (uuid == null) {
                res.removeCookie("skinhub_session");
                res.redirect("/login");
                spark.Spark.halt();
            }

            req.attribute("uuid", uuid);
        };
    }

    private String loadTemplate(String name) {
        try (java.io.InputStream is = getClass().getResourceAsStream("/web/templates/" + name)) {
            if (is == null) return "Template not found";
            return new String(is.readAllBytes());
        } catch (Exception e) {
            return "Error loading template";
        }
    }
}