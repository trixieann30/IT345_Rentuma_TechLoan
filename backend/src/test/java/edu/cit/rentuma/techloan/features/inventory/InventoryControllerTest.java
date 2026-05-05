package edu.cit.rentuma.techloan.features.inventory;

import edu.cit.rentuma.techloan.AbstractIntegrationTest;
import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.inventory.model.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TC-INV: Inventory Controller Tests")
class InventoryControllerTest extends AbstractIntegrationTest {

    // TC-INV-001
    @Test
    @DisplayName("TC-INV-001: GET /inventory/available returns list of available items")
    void getAvailableItems_returnsOnlyAvailable() throws Exception {
        User student = createStudent();
        createAvailableItem();

        InventoryItem unavailable = new InventoryItem("CAM-001", "Camera", "Test camera", "Camera");
        unavailable.setAvailable(false);
        unavailable.setTotalQuantity(1);
        unavailable.setAvailableQuantity(0);
        unavailable.setCreatedAt(LocalDateTime.now());
        unavailable.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(unavailable);

        mockMvc.perform(get("/api/inventory/available")
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    // TC-INV-002
    @Test
    @DisplayName("TC-INV-002: GET /inventory/all returns all items regardless of availability")
    void getAllItems_returnsBothAvailableAndUnavailable() throws Exception {
        User student = createStudent();
        createAvailableItem();

        InventoryItem unavailable = new InventoryItem("CAM-002", "Camera2", "Test camera 2", "Camera");
        unavailable.setAvailable(false);
        unavailable.setTotalQuantity(1);
        unavailable.setAvailableQuantity(0);
        unavailable.setCreatedAt(LocalDateTime.now());
        unavailable.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(unavailable);

        mockMvc.perform(get("/api/inventory/all")
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // TC-INV-003
    @Test
    @DisplayName("TC-INV-003: GET /inventory/{id} returns item when found")
    void getById_existingItem_returns200() throws Exception {
        User student = createStudent();
        InventoryItem item = createAvailableItem();

        mockMvc.perform(get("/api/inventory/" + item.getId())
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("Test Laptop"));
    }

    // TC-INV-004
    @Test
    @DisplayName("TC-INV-004: GET /inventory/{id} returns 404 when not found")
    void getById_nonExistentItem_returns404() throws Exception {
        User student = createStudent();

        mockMvc.perform(get("/api/inventory/99999")
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isNotFound());
    }

    // TC-INV-005
    @Test
    @DisplayName("TC-INV-005: POST /inventory by custodian creates item and returns 201")
    void createItem_asCustodian_returns201() throws Exception {
        User custodian = createCustodian();

        Map<String, Object> body = Map.of(
                "name", "New Projector",
                "description", "Epson projector",
                "category", "Projector",
                "condition", "Excellent",
                "quantity", 2,
                "specifications", "4K, HDMI"
        );

        mockMvc.perform(post("/api/inventory")
                        .header("Authorization", custodianToken(custodian))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemName").value("New Projector"))
                .andExpect(jsonPath("$.availableQuantity").value(2));
    }

    // TC-INV-006
    @Test
    @DisplayName("TC-INV-006: POST /inventory by student returns 403 Forbidden")
    void createItem_asStudent_returns403() throws Exception {
        User student = createStudent();

        Map<String, Object> body = Map.of(
                "name", "Unauthorized Item",
                "description", "test",
                "category", "Laptop",
                "condition", "Good",
                "quantity", 1,
                "specifications", "specs"
        );

        mockMvc.perform(post("/api/inventory")
                        .header("Authorization", studentToken(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // TC-INV-007
    @Test
    @DisplayName("TC-INV-007: PUT /inventory/{id} by custodian updates item and returns 200")
    void updateItem_asCustodian_returns200() throws Exception {
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();

        Map<String, Object> body = Map.of("name", "Updated Laptop");

        mockMvc.perform(put("/api/inventory/" + item.getId())
                        .header("Authorization", custodianToken(custodian))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemName").value("Updated Laptop"));
    }

    // TC-INV-008
    @Test
    @DisplayName("TC-INV-008: DELETE /inventory/{id} by custodian returns 200 with message")
    void deleteItem_asCustodian_returns200() throws Exception {
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();

        mockMvc.perform(delete("/api/inventory/" + item.getId())
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item deleted successfully"));
    }
}
