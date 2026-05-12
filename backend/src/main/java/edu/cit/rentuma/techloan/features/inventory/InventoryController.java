package edu.cit.rentuma.techloan.features.inventory;

import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.inventory.dto.CreateInventoryItemDTO;
import edu.cit.rentuma.techloan.features.inventory.dto.InventoryItemDTO;
import edu.cit.rentuma.techloan.features.inventory.dto.UpdateInventoryItemDTO;
import edu.cit.rentuma.techloan.features.inventory.model.InventoryItem;
import edu.cit.rentuma.techloan.features.inventory.repository.InventoryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    public InventoryController(InventoryRepository inventoryRepository,
                                UserRepository userRepository) {
        this.inventoryRepository = inventoryRepository;
        this.userRepository      = userRepository;
    }

    @GetMapping("/available")
    public ResponseEntity<List<InventoryItemDTO>> getAvailableItems() {
        return ResponseEntity.ok(toDTOList(inventoryRepository.findByAvailableTrue()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryItemDTO>> getAllItems() {
        return ResponseEntity.ok(toDTOList(inventoryRepository.findAll()));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<InventoryItemDTO>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(toDTOList(inventoryRepository.findByCategory(category)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDTO> getById(@PathVariable Long id) {
        return inventoryRepository.findById(id)
                .map(item -> ResponseEntity.ok(toDTO(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<InventoryItemDTO>> search(@RequestParam String query) {
        return ResponseEntity.ok(
                toDTOList(inventoryRepository.findByItemNameContainingIgnoreCase(query)));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = inventoryRepository.findAll().stream()
                .map(InventoryItem::getCategory)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<?> createItem(
            @Valid @RequestBody CreateInventoryItemDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can add inventory items"));
        }

        InventoryItem item = new InventoryItem();
        item.setItemCode("ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        item.setItemName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setCondition(request.getCondition());
        item.setTotalQuantity(request.getQuantity());
        item.setAvailableQuantity(request.getQuantity());
        item.setAvailable(request.getQuantity() > 0);
        item.setSpecifications(request.getSpecifications());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(inventoryRepository.save(item)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryItemDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can update inventory items"));
        }

        return inventoryRepository.findById(id).map(item -> {
            if (request.getName() != null)           item.setItemName(request.getName());
            if (request.getDescription() != null)    item.setDescription(request.getDescription());
            if (request.getCategory() != null)       item.setCategory(request.getCategory());
            if (request.getCondition() != null)      item.setCondition(request.getCondition());
            if (request.getSpecifications() != null) item.setSpecifications(request.getSpecifications());
            if (request.getAvailable() != null)      item.setAvailable(request.getAvailable());

            if (request.getQuantity() != null) {
                item.setTotalQuantity(request.getQuantity());
                if (item.getAvailableQuantity() > request.getQuantity()) {
                    item.setAvailableQuantity(request.getQuantity());
                }
                if (request.getQuantity() == 0) item.setAvailable(false);
            }

            item.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(toDTO(inventoryRepository.save(item)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!isCustodian(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only custodians can delete inventory items"));
        }

        if (!inventoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        inventoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Item deleted successfully", "id", id));
    }

    private boolean isCustodian(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(u -> u.getRole() == User.Role.CUSTODIAN)
                .orElse(false);
    }

    private List<InventoryItemDTO> toDTOList(List<InventoryItem> items) {
        return items.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private InventoryItemDTO toDTO(InventoryItem item) {
        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setId(item.getId());
        dto.setItemCode(item.getItemCode());
        dto.setItemName(item.getItemName());
        dto.setDescription(item.getDescription());
        dto.setCategory(item.getCategory());
        dto.setCondition(item.getCondition());
        dto.setAvailable(item.getAvailable());
        dto.setTotalQuantity(item.getTotalQuantity());
        dto.setAvailableQuantity(item.getAvailableQuantity());
        dto.setSpecifications(item.getSpecifications());
        dto.setImageUrl(item.getImageUrl());
        dto.setUserProvidedImage(Boolean.TRUE.equals(item.getUserProvidedImage()));
        return dto;
    }
}
