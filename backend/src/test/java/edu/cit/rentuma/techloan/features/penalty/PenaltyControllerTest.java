package edu.cit.rentuma.techloan.features.penalty;

import edu.cit.rentuma.techloan.AbstractIntegrationTest;
import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.inventory.model.InventoryItem;
import edu.cit.rentuma.techloan.features.loan.model.Loan;
import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TC-PEN: Penalty Controller Tests")
class PenaltyControllerTest extends AbstractIntegrationTest {

    // TC-PEN-001
    @Test
    @DisplayName("TC-PEN-001: Custodian GET /users/{id}/penalties returns penalty summary")
    void getPenalties_asCustodian_returns200() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();
        Loan loan = saveOverdueLoan(student, item);
        savePenalty(student, loan, 5, 5);

        mockMvc.perform(get("/api/users/" + student.getId() + "/penalties")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(student.getId()))
                .andExpect(jsonPath("$.totalPoints").value(5))
                .andExpect(jsonPath("$.penalties", hasSize(1)));
    }

    // TC-PEN-002
    @Test
    @DisplayName("TC-PEN-002: Student GET /users/{id}/penalties for own ID returns 200")
    void getPenalties_asOwn_returns200() throws Exception {
        User student = createStudent();
        InventoryItem item = createAvailableItem();
        Loan loan = saveOverdueLoan(student, item);
        savePenalty(student, loan, 3, 3);

        mockMvc.perform(get("/api/users/" + student.getId() + "/penalties")
                        .header("Authorization", studentToken(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(student.getId()));
    }

    // TC-PEN-003
    @Test
    @DisplayName("TC-PEN-003: Student GET /users/{otherId}/penalties returns 403")
    void getPenalties_asOtherStudent_returns403() throws Exception {
        User student1 = createStudent("s1@cit.edu", "S-001");
        User student2 = createStudent("s2@cit.edu", "S-002");

        mockMvc.perform(get("/api/users/" + student1.getId() + "/penalties")
                        .header("Authorization", studentToken(student2)))
                .andExpect(status().isForbidden());
    }

    // TC-PEN-007
    @Test
    @DisplayName("TC-PEN-007: Penalty summary totalPoints sums only unpaid penalties")
    void getPenalties_totalPoints_sumsOnlyUnpaid() throws Exception {
        User student = createStudent();
        User custodian = createCustodian();
        InventoryItem item = createAvailableItem();

        Loan loan1 = saveOverdueLoan(student, item);
        Loan loan2 = saveOverdueLoan(student, item);

        savePenalty(student, loan1, 10, 10);
        Penalty paidPenalty = savePenalty(student, loan2, 5, 5);
        paidPenalty.setPaid(true);
        penaltyRepository.save(paidPenalty);

        mockMvc.perform(get("/api/users/" + student.getId() + "/penalties")
                        .header("Authorization", custodianToken(custodian)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(10));
    }

    private Loan saveOverdueLoan(User user, InventoryItem item) {
        BorrowRequest req = new BorrowRequest();
        req.setUserId(user.getId());
        req.setUserEmail(user.getEmail());
        req.setInventoryId(item.getId());
        req.setQuantity(1);
        req.setPurpose("Test");
        req.setItemName(item.getItemName());
        req.setItemDescription(item.getDescription());
        req.setReturnDate(LocalDate.now().minusDays(5));
        req.setStatus(BorrowStatus.OVERDUE);
        req.setBorrowDate(LocalDateTime.now().minusDays(10));
        BorrowRequest saved = borrowRequestRepository.save(req);

        Loan loan = new Loan();
        loan.setReservationId(saved.getId());
        loan.setUserId(user.getId());
        loan.setInventoryId(item.getId());
        loan.setItemName(item.getItemName());
        loan.setQuantity(1);
        loan.setBorrowedAt(LocalDateTime.now().minusDays(10));
        loan.setDueDate(LocalDate.now().minusDays(5));
        loan.setIsOverdue(true);
        return loanRepository.save(loan);
    }

    private Penalty savePenalty(User user, Loan loan, int points, int daysOverdue) {
        Penalty p = new Penalty();
        p.setLoanId(loan.getId());
        p.setUserId(user.getId());
        p.setItemName(loan.getItemName());
        p.setPenaltyPoints(points);
        p.setDaysOverdue(daysOverdue);
        p.setCalculatedAt(LocalDateTime.now());
        p.setPaid(false);
        return penaltyRepository.save(p);
    }
}
