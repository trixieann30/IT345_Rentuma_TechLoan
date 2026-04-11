package edu.cit.rentuma.techloan.controller;

import edu.cit.rentuma.techloan.model.InventoryItem;
import edu.cit.rentuma.techloan.model.User;
import edu.cit.rentuma.techloan.repository.InventoryRepository;
import edu.cit.rentuma.techloan.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Handles image upload for inventory items.
 * POST /api/inventory/{id}/upload-image
 *
 * Stores the file under /uploads/equipment/ on the server,
 * saves the public URL path in inventory_items.image_url,
 * and returns { itemId, imageUrl }.
 *
 * Only CUSTODIAN role may call this endpoint.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryImageController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final String UPLOAD_DIR = "uploads/equipment/";

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    public InventoryImageController(InventoryRepository inventoryRepository,
                                    UserRepository userRepository) {
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/inventory/{id}/upload-image
     * Body: multipart/form-data, field name = "image"
     * Accepted: jpg, jpeg, png — max 5 MB
     */
    @PostMapping(value = "/{id}/upload-image", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        // --- Role guard ---
        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can upload equipment images"));
        }

        // --- Item existence check ---
        InventoryItem item = inventoryRepository.findById(id).orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Inventory item not found"));
        }

        // --- File validation ---
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No file provided"));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size exceeds 5MB limit"));
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid file name"));
        }

        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only jpg, jpeg, and png files are allowed"));
        }

        // --- Save file to disk ---
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename to avoid collisions
            String filename = "item-" + id + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // Public URL path served by Spring's resource handler
            String imageUrl = "/uploads/equipment/" + filename;

            // Persist to DB
            item.setImageUrl(imageUrl);
            inventoryRepository.save(item);

            return ResponseEntity.ok(Map.of(
                    "itemId", id,
                    "imageUrl", imageUrl
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save image: " + e.getMessage()));
        }
    }

    // --- Helpers ---

    private boolean isCustodian(UserDetails userDetails) {
        if (userDetails == null) return false;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        return user != null && user.getRole() == User.Role.CUSTODIAN;
    }
}