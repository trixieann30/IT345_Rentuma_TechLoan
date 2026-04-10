package edu.cit.rentuma.techloan.observer;

/**
 * Refactoring 4 – Observer Pattern (Behavioral):
 *
 * Enum representing the possible states of a BorrowRequest throughout
 * its lifecycle. Transitions are managed by LoanService, and side effects
 * (penalties, audit logs) are handled by event listeners.
 *
 * States:
 *   PENDING – Request created, awaiting custodian approval
 *   APPROVED – Custodian approved; item handed to borrower
 *   RETURNED – Borrower returned the item on time
 *   OVERDUE – Item was not returned by the due date
 */
public enum BorrowStatus {
    PENDING,
    APPROVED,
    RETURNED,
    OVERDUE
}
