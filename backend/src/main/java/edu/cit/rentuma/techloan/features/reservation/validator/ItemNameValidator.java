package edu.cit.rentuma.techloan.features.reservation.validator;

import edu.cit.rentuma.techloan.features.reservation.dto.CreateBorrowRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class ItemNameValidator extends BorrowRequestValidator {
    @Override
    protected void doValidate(CreateBorrowRequestDTO request) throws ValidationException {
        if (request.getInventoryId() == null || request.getInventoryId() <= 0) {
            throw new ValidationException("Valid inventory item ID is required");
        }
    }
}
