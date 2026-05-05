package edu.cit.rentuma.techloan.features.reservation;

import edu.cit.rentuma.techloan.AbstractIntegrationTest;
import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.inventory.model.InventoryItem;
import edu.cit.rentuma.techloan.features.reservation.dto.CreateBorrowRequestDTO;
import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TC-RES: Reservation Controller Tests")
class BorrowControllerTest extends AbstractIntegrationTest {

    // TC-RES-001
    @Test
    @DisplayName("TC-RES-001: Student submits valid reservation request and gets 201")
    void createReservation_asStudent_returns201() throws Exception {
        User student = createStudent();
        InventoryItem item = createAvailableItem();

        CreateBorrowRequestDTO req = new CreateBorrowRequestDTO(
                item.getId(), 1, "Research project", LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", studentToken(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.inventoryId").value(item.getId()));
    }

    // TC-RES-002
    @Test
    @DisplayName("TC-RES-002: Custodian submitting reservation returns 403")
    void createReservation_asCustodian_returns403() throws Exception {
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();

        CreateBorrowRequestDTO req = new CreateBorrowRequestDTO(
                item.getId(), 1, "Purpose", LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", custodianToken(custodian))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // TC-RES-003
    @Test
    @DisplayName("TC-RES-003: Reservation for non-existent item returns 404")
    void createReservation_itemNotFound_returns404() throws Exception {
        User student = createStudent();

        CreateBorrowRequestDTO req = new CreateBorrowRequestDTO(
                99999L, 1, "Purpose", LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", studentToken(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // TC-RES-004
    @Test
    @DisplayName("TC-RES-004: Reservation when quantity exceeds availability returns 409")
    void createReservation_insufficientQuantity_returns409() throws Exception {
        User student = createStudent();
        InventoryItem item = createAvailableItem();
        item.setAvailableQuantity(0);
        item.setAvailable(false);
        inventoryRepository.save(item);

        CreateBorrowRequestDTO req = new CreateBorrowRequestDTO(
                item.getId(), 1, "Purpose", LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", studentToken(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // TC-RES-005
    @Test
    @DisplayName("TC-RES-005: Reservation with null inventoryId triggers validator and returns 400")
    void createReservation_nullInventoryId_returns400() throws Exception {
        User student = createStudent();

        String body = "{\"inventoryId\":null,\"quantity\":1,\"purpose\":\"test\",\"returnDate\":\"" +
                LocalDate.now().plusDays(5) + "\"}";

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", studentToken(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // TC-RES-006
    @Test
    @DisplayName("TC-RES-006: Reservation with past return date returns 400")
    void createReservation_pastReturnDate_returns400() throws Exception {
        User student = createStudent();
        InventoryItem item = createAvailableItem();

        String body = "{\"inventoryId\":" + item.getId() +
                ",\"quantity\":1,\"purpose\":\"test\",\"returnDate\":\"2020-01-01\"}";

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", studentToken(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // TC-RES-007
    @Test
    @DisplayName("TC-RES-007: Custodian GET /reservations returns all reservations")
    void getReservations_asCustodian_returnsAll() throws Exception {
        User student1 = createStudent("s1@cit.edu", "S-001");
        User student2 = createStudent("s2@cit.edu", "S-002");
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();

        saveBorrowRequest(student1, item);
        saveBorrowRequest(student2, item);

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // TC-RES-008
    @Test
    @DisplayName("TC-RES-008: Student GET /reservations returns only own reservations")
    void getReservations_asStudent_returnsOwn() throws Exception {
        User student1 = createStudent("s1@cit.edu", "S-001");
        User student2 = createStudent("s2@cit.edu", "S-002");
        InventoryItem item = createAvailableItem();

        saveBorrowRequest(student1, item);
        saveBorrowRequest(student2, item);

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", studentToken(student1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userEmail").value("s1@cit.edu"));
    }

    // TC-RES-009
    @Test
    @DisplayName("TC-RES-009: GET /reservations/{id} by owner returns 200")
    void getReservation_byOwner_returns200() throws Exception {
        User student = createStudent();
        InventoryItem item = createAvailableItem();
        BorrowRequest req = saveBorrowRequest(student, item);

        mockMvc.perform(get("/api/reservations/" + req.getId())
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(req.getId()));
    }

    // TC-RES-010
    @Test
    @DisplayName("TC-RES-010: GET /reservations/{id} by another student returns 403")
    void getReservation_byOtherStudent_returns403() throws Exception {
        User student1 = createStudent("s1@cit.edu", "S-001");
        User student2 = createStudent("s2@cit.edu", "S-002");
        InventoryItem item = createAvailableItem();
        BorrowRequest req = saveBorrowRequest(student1, item);

        mockMvc.perform(get("/api/reservations/" + req.getId())
                        .header("Authorization", studentToken(student2)))
                .andExpect(status().isForbidden());
    }

    // TC-RES-011
    @Test
    @DisplayName("TC-RES-011: Custodian approves reservation, status becomes APPROVED")
    void approveReservation_asCustodian_returnsApproved() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();
        BorrowRequest req = saveBorrowRequest(student, item);

        mockMvc.perform(put("/api/reservations/" + req.getId() + "/approve")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // TC-RES-012
    @Test
    @DisplayName("TC-RES-012: Custodian rejects reservation, status becomes REJECTED")
    void rejectReservation_asCustodian_returnsRejected() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();
        BorrowRequest req = saveBorrowRequest(student, item);

        mockMvc.perform(put("/api/reservations/" + req.getId() + "/reject")
                        .header("Authorization", custodianToken(custodian))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    // TC-RES-013
    @Test
    @DisplayName("TC-RES-013: Custodian processes return, status becomes RETURNED")
    void returnReservation_asCustodian_returnsReturned() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();
        BorrowRequest req = saveBorrowRequest(student, item);

        mockMvc.perform(put("/api/reservations/" + req.getId() + "/return")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }

    // TC-RES-014
    @Test
    @DisplayName("TC-RES-014: Custodian marks reservation as OVERDUE")
    void markOverdue_asCustodian_returnsOverdue() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();
        BorrowRequest req = saveBorrowRequest(student, item);

        mockMvc.perform(put("/api/reservations/" + req.getId() + "/overdue")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OVERDUE"));
    }

    // TC-RES-015
    @Test
    @DisplayName("TC-RES-015: Student attempting to approve returns 403")
    void approveReservation_asStudent_returns403() throws Exception {
        User student = createStudent();
        InventoryItem item = createAvailableItem();
        BorrowRequest req = saveBorrowRequest(student, item);

        mockMvc.perform(put("/api/reservations/" + req.getId() + "/approve")
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isForbidden());
    }

    private BorrowRequest saveBorrowRequest(User user, InventoryItem item) {
        BorrowRequest req = new BorrowRequest();
        req.setUserId(user.getId());
        req.setUserEmail(user.getEmail());
        req.setInventoryId(item.getId());
        req.setQuantity(1);
        req.setPurpose("Test purpose");
        req.setItemName(item.getItemName());
        req.setItemDescription(item.getDescription());
        req.setReturnDate(LocalDate.now().plusDays(7));
        req.setStatus(BorrowStatus.PENDING);
        req.setBorrowDate(LocalDateTime.now());
        return borrowRequestRepository.save(req);
    }
}
