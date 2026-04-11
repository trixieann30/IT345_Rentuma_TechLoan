package edu.cit.rentuma.techloan.validator;

import edu.cit.rentuma.techloan.dto.CreateBorrowRequestDTO;

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
