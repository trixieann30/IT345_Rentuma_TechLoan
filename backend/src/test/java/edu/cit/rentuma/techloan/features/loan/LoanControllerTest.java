package edu.cit.rentuma.techloan.features.loan;

import edu.cit.rentuma.techloan.AbstractIntegrationTest;
import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.inventory.model.InventoryItem;
import edu.cit.rentuma.techloan.features.loan.model.Loan;
import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TC-LOAN: Loan Controller Tests")
class LoanControllerTest extends AbstractIntegrationTest {

    // TC-LOAN-001
    @Test
    @DisplayName("TC-LOAN-001: Custodian GET /loans returns all loans")
    void getLoans_asCustodian_returnsAll() throws Exception {
        User student1 = createStudent("s1@cit.edu", "S-001");
        User student2 = createStudent("s2@cit.edu", "S-002");
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();

        saveApprovedLoan(student1, item);
        saveApprovedLoan(student2, item);

        mockMvc.perform(get("/api/loans")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // TC-LOAN-002
    @Test
    @DisplayName("TC-LOAN-002: Student GET /loans returns only their own loans")
    void getLoans_asStudent_returnsOwn() throws Exception {
        User student1 = createStudent("s1@cit.edu", "S-001");
        User student2 = createStudent("s2@cit.edu", "S-002");
        InventoryItem item = createAvailableItem();

        saveApprovedLoan(student1, item);
        saveApprovedLoan(student2, item);

        mockMvc.perform(get("/api/loans")
                        .header("Authorization", studentToken(student1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(student1.getId()));
    }

    // TC-LOAN-003
    @Test
    @DisplayName("TC-LOAN-003: GET /loans?isOverdue=true returns only overdue loans")
    void getLoans_filterOverdue_returnsOnlyOverdue() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();

        Loan normalLoan = saveApprovedLoan(student, item);
        normalLoan.setIsOverdue(false);
        loanRepository.save(normalLoan);

        Loan overdueLoan = saveApprovedLoan(student, item);
        overdueLoan.setIsOverdue(true);
        loanRepository.save(overdueLoan);

        mockMvc.perform(get("/api/loans?isOverdue=true")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].isOverdue", everyItem(is(true))));
    }

    // TC-LOAN-004
    @Test
    @DisplayName("TC-LOAN-004: Custodian POST /loans/{id}/return marks loan as returned")
    void returnLoan_asCustodian_returns200() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();
        Loan loan = saveApprovedLoan(student, item);

        mockMvc.perform(post("/api/loans/" + loan.getId() + "/return")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnedAt").isNotEmpty());
    }

    // TC-LOAN-005
    @Test
    @DisplayName("TC-LOAN-005: Student POST /loans/{id}/return returns 403")
    void returnLoan_asStudent_returns403() throws Exception {
        User student = createStudent();
        InventoryItem item = createAvailableItem();
        Loan loan = saveApprovedLoan(student, item);

        mockMvc.perform(post("/api/loans/" + loan.getId() + "/return")
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isForbidden());
    }

    // TC-LOAN-006
    @Test
    @DisplayName("TC-LOAN-006: Approving a reservation via PUT /reservations/{id}/approve creates a loan")
    void approveReservation_createsLoanRecord() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();
        BorrowRequest req = savePendingReservation(student, item);

        mockMvc.perform(put("/api/reservations/" + req.getId() + "/approve")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk());

        long loanCount = loanRepository.findAll().stream()
                .filter(l -> l.getReservationId().equals(req.getId()))
                .count();
        org.junit.jupiter.api.Assertions.assertEquals(1, loanCount);
    }

    // TC-LOAN-007
    @Test
    @DisplayName("TC-LOAN-007: Returning a loan sets returnedAt and isOverdue to false")
    void returnLoan_setsReturnedAtAndClearsOverdue() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();

        Loan loan = saveApprovedLoan(student, item);
        loan.setIsOverdue(true);
        loanRepository.save(loan);

        mockMvc.perform(post("/api/loans/" + loan.getId() + "/return")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isOverdue").value(false));
    }

    private Loan saveApprovedLoan(User user, InventoryItem item) {
        BorrowRequest req = savePendingReservation(user, item);
        Loan loan = new Loan();
        loan.setReservationId(req.getId());
        loan.setUserId(user.getId());
        loan.setInventoryId(item.getId());
        loan.setItemName(item.getItemName());
        loan.setQuantity(1);
        loan.setBorrowedAt(LocalDateTime.now());
        loan.setDueDate(LocalDate.now().plusDays(7));
        loan.setIsOverdue(false);
        return loanRepository.save(loan);
    }

    private BorrowRequest savePendingReservation(User user, InventoryItem item) {
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
