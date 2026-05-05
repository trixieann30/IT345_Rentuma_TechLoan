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
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TC-PEN: Penalty Calculation Service Tests")
class PenaltyCalculationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private PenaltyCalculationService penaltyCalculationService;

    // TC-PEN-004
    @Test
    @DisplayName("TC-PEN-004: calculatePenaltyForLoan creates penalty record for overdue loan")
    void calculatePenaltyForLoan_overdueLoan_createsPenalty() {
        User student = createStudent();
        InventoryItem item = createAvailableItem();
        Loan loan = saveOverdueLoan(student, item, 5);

        penaltyCalculationService.calculatePenaltyForLoan(loan.getId());

        Optional<Penalty> penalty = penaltyRepository.findByLoanId(loan.getId());
        assertTrue(penalty.isPresent());
        assertEquals(5, penalty.get().getPenaltyPoints());
        assertEquals(5, penalty.get().getDaysOverdue());
    }

    // TC-PEN-005
    @Test
    @DisplayName("TC-PEN-005: Penalty points are capped at 30 even when overdue by more than 30 days")
    void calculatePenaltyForLoan_over30Days_capsAt30() {
        User student = createStudent();
        InventoryItem item = createAvailableItem();
        Loan loan = saveOverdueLoan(student, item, 45);

        penaltyCalculationService.calculatePenaltyForLoan(loan.getId());

        Optional<Penalty> penalty = penaltyRepository.findByLoanId(loan.getId());
        assertTrue(penalty.isPresent());
        assertEquals(30, penalty.get().getPenaltyPoints());
    }

    // TC-PEN-006
    @Test
    @DisplayName("TC-PEN-006: calculatePenaltyForLoan does not create penalty for non-overdue loan")
    void calculatePenaltyForLoan_notOverdue_noPenalty() {
        User student = createStudent();
        InventoryItem item = createAvailableItem();

        BorrowRequest req = new BorrowRequest();
        req.setUserId(student.getId());
        req.setUserEmail(student.getEmail());
        req.setInventoryId(item.getId());
        req.setQuantity(1);
        req.setPurpose("Test");
        req.setItemName(item.getItemName());
        req.setItemDescription(item.getDescription());
        req.setReturnDate(LocalDate.now().plusDays(7));
        req.setStatus(BorrowStatus.APPROVED);
        req.setBorrowDate(LocalDateTime.now());
        BorrowRequest savedReq = borrowRequestRepository.save(req);

        Loan loan = new Loan();
        loan.setReservationId(savedReq.getId());
        loan.setUserId(student.getId());
        loan.setInventoryId(item.getId());
        loan.setItemName(item.getItemName());
        loan.setQuantity(1);
        loan.setBorrowedAt(LocalDateTime.now());
        loan.setDueDate(LocalDate.now().plusDays(7));
        loan.setIsOverdue(false);
        loan = loanRepository.save(loan);

        penaltyCalculationService.calculatePenaltyForLoan(loan.getId());

        Optional<Penalty> penalty = penaltyRepository.findByLoanId(loan.getId());
        assertFalse(penalty.isPresent());
    }

    private Loan saveOverdueLoan(User user, InventoryItem item, int daysOverdue) {
        BorrowRequest req = new BorrowRequest();
        req.setUserId(user.getId());
        req.setUserEmail(user.getEmail());
        req.setInventoryId(item.getId());
        req.setQuantity(1);
        req.setPurpose("Test");
        req.setItemName(item.getItemName());
        req.setItemDescription(item.getDescription());
        req.setReturnDate(LocalDate.now().minusDays(daysOverdue));
        req.setStatus(BorrowStatus.OVERDUE);
        req.setBorrowDate(LocalDateTime.now().minusDays(daysOverdue + 5));
        BorrowRequest savedReq = borrowRequestRepository.save(req);

        Loan loan = new Loan();
        loan.setReservationId(savedReq.getId());
        loan.setUserId(user.getId());
        loan.setInventoryId(item.getId());
        loan.setItemName(item.getItemName());
        loan.setQuantity(1);
        loan.setBorrowedAt(LocalDateTime.now().minusDays(daysOverdue + 5));
        loan.setDueDate(LocalDate.now().minusDays(daysOverdue));
        loan.setIsOverdue(false);
        return loanRepository.save(loan);
    }
}
