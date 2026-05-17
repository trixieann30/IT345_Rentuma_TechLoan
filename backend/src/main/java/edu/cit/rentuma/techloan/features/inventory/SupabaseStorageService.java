package edu.cit.rentuma.techloan.features.inventory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SupabaseStorageService {

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.service-key:}")
    private String serviceKey;

    @Value("${app.supabase.anon-key}")
    private String anonKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String uploadImage(String bucket, String filename, byte[] bytes, String contentType) throws Exception {
        String key = (serviceKey != null && !serviceKey.isBlank()) ? serviceKey : anonKey;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filename;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", contentType)
                .header("x-upsert", "true")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Supabase upload failed: HTTP " + response.statusCode() + " — " + response.body());
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filename;
    }
}
