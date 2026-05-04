package edu.cit.rentuma.techloan.features.reservation.observer;

import java.io.Serializable;

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

    public Long getBorrowRequestId() { return borrowRequestId; }
    public BorrowStatus getNewStatus() { return newStatus; }
    public String getUserEmail()      { return userEmail; }
}
