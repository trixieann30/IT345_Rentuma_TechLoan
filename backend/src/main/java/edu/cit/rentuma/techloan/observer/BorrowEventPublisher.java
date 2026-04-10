package edu.cit.rentuma.techloan.observer;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * The Subject/Publisher that wraps Spring's ApplicationEventPublisher
 * and provides a domain-friendly API for firing borrow-status events.
 *
 * Why this matters for TechLoan:
 *   - LoanService calls a single method here; it knows nothing about
 *     penalties, audit logs, or notifications.
 *   - Any number of @EventListener beans can react without touching
 *     LoanService or this class.
 *   - Swapping the underlying event bus (e.g., to a message broker)
 *     only requires changes here, not across all call sites.
 */
@Component
public class BorrowEventPublisher {

    private final ApplicationEventPublisher publisher;

    public BorrowEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publishes a {@link BorrowStatusChangedEvent} to all registered
     * Spring event listeners.
     *
     * @param borrowRequestId the ID of the BorrowRequest that changed
     * @param newStatus       the status the request transitioned into
     * @param userEmail       the email of the borrower
     */
    public void publishStatusChange(Long borrowRequestId,
                                    BorrowStatus newStatus,
                                    String userEmail) {
        publisher.publishEvent(
                new BorrowStatusChangedEvent(borrowRequestId, newStatus, userEmail));
    }
}