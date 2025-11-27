package zeroends.skinhub.web;

import spark.Service;
import zeroends.skinhub.SkinHub;
import zeroends.skinhub.web.routes.ApiRoute;
import zeroends.skinhub.web.routes.AuthRoute;
import zeroends.skinhub.web.routes.DashboardRoute;

import java.io.File;

import static spark.Service.ignite;

public class WebServer {

    private final SkinHub plugin;
    private Service http;

    public WebServer(SkinHub plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int port = plugin.getConfig().getInt("web-server.port", 25566);
        String bindAddress = plugin.getConfig().getString("web-server.bind-address", "0.0.0.0");
        
        http = ignite()
                .port(port)
                .ipAddress(bindAddress)
                .threadPool(10); 

        // SSL Configuration
        if (plugin.getConfig().getBoolean("web-server.ssl.enabled")) {
            String keystorePath = plugin.getConfig().getString("web-server.ssl.keystore-path");
            String keystorePassword = plugin.getConfig().getString("web-server.ssl.keystore-password");
            if (keystorePath != null && keystorePassword != null) {
                File pluginKeystore = new File(plugin.getDataFolder(), keystorePath.replace("plugins/SkinHub/", ""));
                if (pluginKeystore.exists()) {
                     http.secure(pluginKeystore.getAbsolutePath(), keystorePassword, null, null);
                }
            }
        }

        http.staticFiles.location("/web/static");

        AuthRoute authRoute = new AuthRoute(plugin);
        DashboardRoute dashboardRoute = new DashboardRoute(plugin);
        ApiRoute apiRoute = new ApiRoute(plugin);

        http.get("/login", authRoute.handleLogin);
        http.post("/auth/verify", authRoute.handleVerify);
        
        // FIX: Handle GET request ke /auth/verify (redirect kembali ke login untuk mencegah error 404/Not Mapped)
        http.get("/auth/verify", (req, res) -> {
            res.redirect("/login");
            return null;
        });

        // FIX: Handle Favicon request agar tidak spam error di console
        http.get("/favicon.ico", (req, res) -> {
            res.status(404);
            return "";
        });
        
        http.get("/manage", dashboardRoute.serveDashboard);
        
        http.path("/api", () -> {
            http.before("/*", authRoute.requireAuth);
            http.post("/upload", apiRoute.handleUpload);
            http.post("/fetch", apiRoute.handleFetch);
            http.post("/apply/:id", apiRoute.handleApply);
            http.delete("/delete/:id", apiRoute.handleDelete);
        });
        
        http.get("/", (req, res) -> {
            res.redirect("/login");
            return null;
        });

        plugin.getLogger().info("Web server started on " + bindAddress + ":" + port);
    }

    public void stop() {
        if (http != null) {
            http.stop();
            http.awaitStop();
        }
    }
}