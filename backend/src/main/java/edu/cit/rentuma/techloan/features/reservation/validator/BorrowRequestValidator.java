package edu.cit.rentuma.techloan.features.reservation.validator;

import edu.cit.rentuma.techloan.features.reservation.dto.CreateBorrowRequestDTO;

public abstract class BorrowRequestValidator {

    protected BorrowRequestValidator next;

    public void setNext(BorrowRequestValidator next) {
        this.next = next;
    }

    public final void validate(CreateBorrowRequestDTO request) throws ValidationException {
        doValidate(request);
        if (next != null) {
            next.validate(request);
        }
    }

    protected abstract void doValidate(CreateBorrowRequestDTO request) throws ValidationException;
}
