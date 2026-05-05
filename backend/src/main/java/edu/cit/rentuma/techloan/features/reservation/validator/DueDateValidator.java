package edu.cit.rentuma.techloan.features.reservation.validator;

import edu.cit.rentuma.techloan.features.reservation.dto.CreateBorrowRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DueDateValidator extends BorrowRequestValidator {
    @Override
    protected void doValidate(CreateBorrowRequestDTO request) throws ValidationException {
        if (request.getReturnDate() == null) {
            throw new ValidationException("Return date is required");
        }
        if (request.getReturnDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Return date must be in the future");
        }
        if (request.getReturnDate().isAfter(LocalDate.now().plusMonths(6))) {
            throw new ValidationException("Return date cannot exceed 6 months from now");
        }
    }
}
