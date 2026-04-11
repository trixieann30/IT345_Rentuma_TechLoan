package edu.cit.rentuma.techloan.observer;

import java.io.Serializable;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Event object published whenever a BorrowRequest changes status.
 * Carrying the request ID, new status, and the affected user's email
 * keeps every downstream listener decoupled from the LoanService that
 * produced the event.
 *
 * This is a plain data-holder (value object). Spring's
 * ApplicationEventPublisher accepts any Object as an event, so no
 * base-class is required.
 */
public class BorrowStatusChangedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long borrowRequestId;
    private final BorrowStatus newStatus;
    private final String userEmail;

    public BorrowStatusChangedEvent(Long borrowRequestId, BorrowStatus newStatus, String userEmail) {
        this.borrowRequestId = borrowRequestId;
        this.newStatus = newStatus;
        this.userEmail = userEmail;
    }

    public Long getBorrowRequestId() {
        return borrowRequestId;
    }

    public BorrowStatus getNewStatus() {
        return newStatus;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
