package edu.cit.rentuma.techloan.validator;

import edu.cit.rentuma.techloan.dto.CreateBorrowRequestDTO;
import org.springframework.stereotype.Component;

/**
 * Validates that purpose (if provided) is reasonable length.
 * Updated to work with the new BorrowRequest DTO structure.
 */
@Component
public class DescriptionValidator extends BorrowRequestValidator {
    @Override
    protected void doValidate(CreateBorrowRequestDTO request) throws ValidationException {
        if (request.getPurpose() != null && request.getPurpose().length() > 500) {
            throw new ValidationException("Purpose must not exceed 500 characters");
        }
    }
}
