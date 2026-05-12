package edu.cit.rentuma.techloan.shared.factory;

import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.loan.dto.LoanDTO;
import edu.cit.rentuma.techloan.features.loan.model.Loan;
import edu.cit.rentuma.techloan.features.penalty.dto.PenaltyDTO;
import edu.cit.rentuma.techloan.features.penalty.model.Penalty;
import edu.cit.rentuma.techloan.features.reservation.dto.BorrowRequestDTO;
import edu.cit.rentuma.techloan.features.reservation.model.BorrowRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class DTOFactory {

    private final UserRepository userRepository;

    public DTOFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public BorrowRequestDTO toBorrowRequestDTO(BorrowRequest borrow) {
        BorrowRequestDTO dto = new BorrowRequestDTO(
                borrow.getId(),
                borrow.getUserId(),
                borrow.getUserEmail(),
                borrow.getInventoryId(),
                borrow.getQuantity(),
                borrow.getPurpose(),
                borrow.getItemName(),
                borrow.getItemDescription(),
                borrow.getStatus(),
                borrow.getBorrowDate(),
                borrow.getDueDate(),
                borrow.getReturnDate(),
                borrow.getActualReturnDate(),
                borrow.getCreatedAt()
        );
        userRepository.findById(borrow.getUserId()).ifPresent(u -> {
            dto.setBorrowerName(u.getFullName());
            dto.setBorrowerRole(u.getRole().name());
        });
        return dto;
    }

    public LoanDTO toLoanDTO(Loan loan, String borrowerName, Integer penaltyPoints) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setReservationId(loan.getReservationId());
        dto.setUserId(loan.getUserId());
        dto.setBorrowerName(borrowerName);
        dto.setInventoryId(loan.getInventoryId());
        dto.setItemName(loan.getItemName());
        dto.setQuantity(loan.getQuantity());
        dto.setBorrowedAt(loan.getBorrowedAt());
        dto.setDueDate(loan.getDueDate());
        dto.setReturnedAt(loan.getReturnedAt());
        dto.setIsOverdue(loan.getIsOverdue());
        dto.setPenaltyPoints(penaltyPoints != null ? penaltyPoints : 0);

        if (loan.getReturnedAt() == null && loan.getDueDate() != null) {
            long days = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
            dto.setDaysOverdue((int) Math.max(0, days));
        } else {
            dto.setDaysOverdue(0);
        }

        return dto;
    }

    public PenaltyDTO toPenaltyDTO(Penalty penalty) {
        PenaltyDTO dto = new PenaltyDTO();
        dto.setId(penalty.getId());
        dto.setLoanId(penalty.getLoanId());
        dto.setItemName(penalty.getItemName());
        dto.setDaysOverdue(penalty.getDaysOverdue());
        dto.setPenaltyPoints(penalty.getPenaltyPoints());
        dto.setCalculatedAt(penalty.getCalculatedAt());
        dto.setPaid(penalty.getPaid());
        return dto;
    }
}
