package edu.cit.rentuma.techloan.observer;

import edu.cit.rentuma.techloan.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Listens for {@link BorrowStatusChangedEvent} and increments the
 * affected user's penalty points when a borrow request becomes OVERDUE.
 *
 * This class has zero knowledge of LoanService. Adding, removing, or
 * modifying penalty logic never requires touching the loan lifecycle.
 */
@Component
public class PenaltyListener {

    private final UserRepository userRepository;

    public PenaltyListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Reacts to borrow-status changes. Only acts when the new status
     * is {@code OVERDUE}; all other transitions are ignored.
     *
     * @param event the event carrying the request ID, new status, and user email
     */
    @EventListener
    public void onStatusChange(BorrowStatusChangedEvent event) {
        if (event.getNewStatus() == BorrowStatus.OVERDUE) {
            userRepository.findByEmail(event.getUserEmail()).ifPresent(user -> {
                int current = user.getPenaltyPoints() != null ? user.getPenaltyPoints() : 0;
                user.setPenaltyPoints(current + 1);
                userRepository.save(user);
            });
        }
    }
}