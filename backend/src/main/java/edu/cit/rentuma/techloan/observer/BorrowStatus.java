package edu.cit.rentuma.techloan.observer;

/**
 * Enum representing the possible states of a BorrowRequest throughout its lifecycle.
 *
 * CHANGED: Added REJECTED state to support PUT /reservations/{id}/reject.
 *
 * States:
 *   PENDING  – Request created, awaiting custodian approval
 *   APPROVED – Custodian approved; item handed to borrower
 *   REJECTED – Custodian rejected the request
 *   RETURNED – Borrower returned the item
 *   OVERDUE  – Item was not returned by the due date
 */
public enum BorrowStatus {
    PENDING,
    APPROVED,
    REJECTED,
    RETURNED,
    OVERDUE
}