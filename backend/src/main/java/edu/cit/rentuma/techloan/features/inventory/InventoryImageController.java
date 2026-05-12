package edu.cit.rentuma.techloan.features.inventory;

import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.inventory.model.InventoryItem;
import edu.cit.rentuma.techloan.features.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryImageController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String UPLOAD_DIR = "uploads/equipment/";

    @Value("${serper.api-key:}")
    private String serperApiKey;

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final RestClient restClient;

    public InventoryImageController(InventoryRepository inventoryRepository,
                                    UserRepository userRepository,
                                    RestClient.Builder builder) {
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.restClient = builder.build();
    }

    @GetMapping("/{id}/auto-image")
    public ResponseEntity<?> autoImage(@PathVariable Long id) {
        InventoryItem item = inventoryRepository.findById(id).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();

        if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
            return ResponseEntity.ok(Map.of("imageUrl", item.getImageUrl()));
        }

        // Custodian chose to provide their own image — never fall back to Serper
        if (Boolean.TRUE.equals(item.getUserProvidedImage())) {
            return ResponseEntity.ok(Map.of("imageUrl", ""));
        }

        if (serperApiKey.isBlank()) {
            return ResponseEntity.ok(Map.of("imageUrl", ""));
        }

        try {
            String category = item.getCategory() != null ? item.getCategory().trim() : "";
            String name     = item.getItemName()  != null ? item.getItemName().trim()  : "";
            // Put category first so generic names ("test") still resolve to the right type of image
            String query = (category.isEmpty() ? name : category + " " + name).trim();

            Map<String, Object> body = Map.of("q", query.trim(), "num", 1);

            Map<?, ?> response = restClient.post()
                    .uri("https://google.serper.dev/images")
                    .header("X-API-KEY", serperApiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<?> images = (List<?>) response.get("images");
            if (images == null || images.isEmpty()) return ResponseEntity.ok(Map.of("imageUrl", ""));

            Map<?, ?> first = (Map<?, ?>) images.get(0);
            String imageUrl = (String) first.get("imageUrl");
            if (imageUrl == null) return ResponseEntity.ok(Map.of("imageUrl", ""));

            item.setImageUrl(imageUrl);
            inventoryRepository.save(item);

            return ResponseEntity.ok(Map.of("itemId", id, "imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("imageUrl", "", "error", e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/upload-image", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can upload equipment images"));
        }

        InventoryItem item = inventoryRepository.findById(id).orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Inventory item not found"));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds 5MB limit"));
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file name"));
        }

        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only jpg, jpeg, and png files are allowed"));
        }

        // Mark as user-provided immediately so Serper is permanently blocked for this item
        item.setUserProvidedImage(true);
        inventoryRepository.save(item);

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Files.createDirectories(uploadPath);

            String filename = "item-" + id + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            Path filePath = uploadPath.resolve(filename);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            String imageUrl = "/uploads/equipment/" + filename;
            item.setImageUrl(imageUrl);
            inventoryRepository.save(item);

            return ResponseEntity.ok(Map.of("itemId", id, "imageUrl", imageUrl));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save image: " + e.getMessage()));
        }
    }

    private boolean isCustodian(UserDetails userDetails) {
        if (userDetails == null) return false;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        return user != null && user.getRole() == User.Role.CUSTODIAN;
    }
}
