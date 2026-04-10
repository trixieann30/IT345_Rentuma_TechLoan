package edu.cit.rentuma.techloan.validator;

import edu.cit.rentuma.techloan.dto.CreateBorrowRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Refactoring 6 – Chain of Responsibility Pattern (Behavioral):
 *
 * Validates borrow requests through a chain of handlers. Each handler
 * checks one responsibility (e.g., item name validation, date validation)
 * and passes to the next handler if valid. This allows:
 *
 *   - Multiple validation steps without conditional nesting
 *   - Easy to add/remove validators without changing request handler
 *   - Each validator has single responsibility
 *   - Validation order is explicit and maintainable
 *
 * Real-world use: Request validation pipelines, logging chains, filtering
 */
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

/**
 * Validates that item name is provided and not empty
 */
@Component
class ItemNameValidator extends BorrowRequestValidator {
    @Override
    protected void doValidate(CreateBorrowRequestDTO request) throws ValidationException {
        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new ValidationException("Item name is required");
        }
        if (request.getItemName().length() > 255) {
            throw new ValidationException("Item name must not exceed 255 characters");
        }
    }
}

/**
 * Validates that due date is in the future
 */
@Component
class DueDateValidator extends BorrowRequestValidator {
    @Override
    protected void doValidate(CreateBorrowRequestDTO request) throws ValidationException {
        if (request.getDueDate() == null) {
            throw new ValidationException("Due date is required");
        }
        if (request.getDueDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Due date must be in the future");
        }
        if (request.getDueDate().isAfter(LocalDateTime.now().plusMonths(6))) {
            throw new ValidationException("Due date cannot exceed 6 months from now");
        }
    }
}

/**
 * Validates that description (if provided) is reasonable
 */
@Component
class DescriptionValidator extends BorrowRequestValidator {
    @Override
    protected void doValidate(CreateBorrowRequestDTO request) throws ValidationException {
        if (request.getItemDescription() != null && request.getItemDescription().length() > 500) {
            throw new ValidationException("Description must not exceed 500 characters");
        }
    }
}

/**
 * Custom exception for validation failures
 */
class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
