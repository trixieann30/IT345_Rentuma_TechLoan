package edu.cit.rentuma.techloan.service.validator;

import edu.cit.rentuma.techloan.dto.RegisterRequest;

/**
 * Refactoring 2 – Strategy Pattern (Behavioral):
 * Defines a common contract for all registration validation strategies.
 * AuthService depends only on this interface, not on any concrete validator.
 */
public interface RegistrationValidator {

    /**
     * Validates the registration request.
     * Throws {@link IllegalArgumentException} or {@link IllegalStateException}
     * with structured error codes if validation fails.
     *
     * @param request the incoming registration data
     */
    void validate(RegisterRequest request);
}