package zeroends.skinhub.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import zeroends.skinhub.model.SkinData;

public class SkinFetcher {

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    // Uses Ashcon API for username lookup as it provides signed textures easily
    public static CompletableFuture<SkinData> fetchSkinFromUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.ashcon.app/mojang/v2/user/" + username))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("Failed to fetch skin from username: " + response.statusCode());
                }

                JsonNode root = mapper.readTree(response.body());
                JsonNode textures = root.path("textures");
                JsonNode skin = textures.path("skin");

                String value = skin.path("data").asText();
                String signature = skin.path("signature").asText();

                return new SkinData(0, username, value, signature);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Uses Mineskin API for image upload to get signed textures
    public static CompletableFuture<SkinData> generateSkinFromImage(File skinFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String boundary = "---" + System.currentTimeMillis() + "---";
                byte[] fileBytes = Files.readAllBytes(skinFile.toPath());

                String header = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"skin.png\"\r\n" +
                        "Content-Type: image/png\r\n\r\n";
                String footer = "\r\n--" + boundary + "--\r\n";

                byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);
                byte[] footerBytes = footer.getBytes(StandardCharsets.UTF_8);

                byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
                System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
                System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
                System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mineskin.org/generate/upload?visibility=0"))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    // Fallback or error logging
                    throw new RuntimeException("Mineskin API Error: " + response.statusCode() + " " + response.body());
                }

                JsonNode root = mapper.readTree(response.body());
                JsonNode data = root.path("data");
                JsonNode texture = data.path("texture");

                String value = texture.path("value").asText();
                String signature = texture.path("signature").asText();
                String name = "Custom-" + UUID.randomUUID().toString().substring(0, 8);

                return new SkinData(0, name, value, signature);

            } catch (Exception e) {
                throw new RuntimeException("Failed to generate skin from image", e);
            }
        });
    }
}