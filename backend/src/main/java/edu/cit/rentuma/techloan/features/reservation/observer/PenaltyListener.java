package edu.cit.rentuma.techloan.features.reservation.observer;

import edu.cit.rentuma.techloan.features.loan.repository.LoanRepository;
import edu.cit.rentuma.techloan.features.penalty.PenaltyCalculationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PenaltyListener {

    private final LoanRepository loanRepository;
    private final PenaltyCalculationService penaltyCalculationService;

    public PenaltyListener(LoanRepository loanRepository,
                           PenaltyCalculationService penaltyCalculationService) {
        this.loanRepository            = loanRepository;
        this.penaltyCalculationService = penaltyCalculationService;
    }

    @EventListener
    public void onStatusChange(BorrowStatusChangedEvent event) {
        if (event.getNewStatus() == BorrowStatus.OVERDUE) {
            loanRepository.findByReservationId(event.getBorrowRequestId())
                    .ifPresent(loan ->
                            penaltyCalculationService.calculatePenaltyForLoan(loan.getId()));
        }
    }
}
