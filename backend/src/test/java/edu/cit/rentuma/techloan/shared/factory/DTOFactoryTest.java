package edu.cit.rentuma.techloan.shared.factory;

import edu.cit.rentuma.techloan.features.loan.dto.LoanDTO;
import edu.cit.rentuma.techloan.features.loan.model.Loan;
import edu.cit.rentuma.techloan.features.penalty.dto.PenaltyDTO;
import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import edu.cit.rentuma.techloan.features.reservation.dto.BorrowRequestDTO;
import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import edu.cit.rentuma.techloan.features.reservation.observer.BorrowStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TC-DP: Factory Pattern — DTOFactory Tests")
class DTOFactoryTest {

    private final DTOFactory dtoFactory = new DTOFactory();

    // TC-DP-004
    @Test
    @DisplayName("TC-DP-004: toBorrowRequestDTO maps all BorrowRequest fields correctly")
    void toBorrowRequestDTO_mapsAllFields() {
        BorrowRequest borrow = new BorrowRequest();
        borrow.setId(null);
        borrow.setUserId(10L);
        borrow.setUserEmail("test@cit.edu");
        borrow.setInventoryId(20L);
        borrow.setQuantity(2);
        borrow.setPurpose("Study");
        borrow.setItemName("Laptop");
        borrow.setItemDescription("A good laptop");
        borrow.setStatus(BorrowStatus.PENDING);
        borrow.setBorrowDate(LocalDateTime.of(2025, 1, 1, 10, 0));
        borrow.setReturnDate(LocalDate.of(2025, 1, 10));

        BorrowRequestDTO dto = dtoFactory.toBorrowRequestDTO(borrow);

        assertEquals(10L, dto.getUserId());
        assertEquals("test@cit.edu", dto.getUserEmail());
        assertEquals(20L, dto.getInventoryId());
        assertEquals(2, dto.getQuantity());
        assertEquals("Study", dto.getPurpose());
        assertEquals("Laptop", dto.getItemName());
        assertEquals(BorrowStatus.PENDING, dto.getStatus());
    }

    // TC-DP-005
    @Test
    @DisplayName("TC-DP-005: toLoanDTO maps all Loan fields and calculates daysOverdue correctly")
    void toLoanDTO_mapsAllFieldsAndCalculatesDaysOverdue() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setReservationId(5L);
        loan.setUserId(10L);
        loan.setInventoryId(20L);
        loan.setItemName("Camera");
        loan.setQuantity(1);
        loan.setBorrowedAt(LocalDateTime.now().minusDays(10));
        loan.setDueDate(LocalDate.now().minusDays(3));
        loan.setReturnedAt(null);
        loan.setIsOverdue(true);

        LoanDTO dto = dtoFactory.toLoanDTO(loan, "Alice", 3);

        assertEquals(1L, dto.getId());
        assertEquals("Alice", dto.getBorrowerName());
        assertEquals("Camera", dto.getItemName());
        assertEquals(3, dto.getPenaltyPoints());
        assertTrue(dto.getDaysOverdue() >= 3);
        assertTrue(dto.getIsOverdue());
    }

    // TC-DP-005b: toPenaltyDTO maps all fields
    @Test
    @DisplayName("TC-DP-005b: toPenaltyDTO maps all Penalty fields correctly")
    void toPenaltyDTO_mapsAllFields() {
        Penalty penalty = new Penalty();
        penalty.setId(1L);
        penalty.setLoanId(5L);
        penalty.setItemName("Projector");
        penalty.setDaysOverdue(7);
        penalty.setPenaltyPoints(7);
        penalty.setCalculatedAt(LocalDateTime.now());
        penalty.setPaid(false);

        PenaltyDTO dto = dtoFactory.toPenaltyDTO(penalty);

        assertEquals(1L, dto.getId());
        assertEquals(5L, dto.getLoanId());
        assertEquals("Projector", dto.getItemName());
        assertEquals(7, dto.getDaysOverdue());
        assertEquals(7, dto.getPenaltyPoints());
        assertFalse(dto.getPaid());
    }
}
