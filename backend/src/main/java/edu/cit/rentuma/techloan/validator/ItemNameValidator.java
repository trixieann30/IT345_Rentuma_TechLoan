package edu.cit.rentuma.techloan.validator;

import edu.cit.rentuma.techloan.dto.CreateBorrowRequestDTO;
import org.springframework.stereotype.Component;

/**
 * Validates that inventory ID is provided and valid.
 * Updated to work with the new BorrowRequest DTO structure.
 */
@Component
public class ItemNameValidator extends BorrowRequestValidator {
    @Override
    protected void doValidate(CreateBorrowRequestDTO request) throws ValidationException {
        if (request.getInventoryId() == null || request.getInventoryId() <= 0) {
            throw new ValidationException("Valid inventory item ID is required");
        }
    }
}
