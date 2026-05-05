package edu.cit.rentuma.techloan.features.reservation.validator;

import edu.cit.rentuma.techloan.features.reservation.dto.CreateBorrowRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class DescriptionValidator extends BorrowRequestValidator {
    @Override
    protected void doValidate(CreateBorrowRequestDTO request) throws ValidationException {
        if (request.getPurpose() != null && request.getPurpose().length() > 500) {
            throw new ValidationException("Purpose must not exceed 500 characters");
        }
    }
}
