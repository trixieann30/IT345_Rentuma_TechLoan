package edu.cit.rentuma.techloan.service.validator;

import edu.cit.rentuma.techloan.dto.RegisterRequest;
import edu.cit.rentuma.techloan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Refactoring 2 – Strategy Pattern (Behavioral):
 * Concrete validation strategy for standard email/password registration.
 *
 * Rules enforced:
 *  1. Passwords match
 *  2. Email belongs to the institutional domain (cit.edu)
 *  3. No duplicate email in the database
 *  4. No duplicate student/faculty ID in the database
 *
 * A GoogleRegistrationValidator (or any future SSO validator) can be
 * created and injected for OAuth flows without touching AuthService.
 */
@Component
@org.springframework.context.annotation.Primary
public class StandardRegistrationValidator implements RegistrationValidator {

    private final UserRepository userRepository;

    @Value("${app.institutional.domain}")
    private String institutionalDomain;

    public StandardRegistrationValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void validate(RegisterRequest request) {
        validatePasswordsMatch(request);
        validateInstitutionalEmail(request);
        validateNoDuplicateEmail(request);
        validateNoDuplicateStudentId(request);
    }

    // ----------------------------------------------------------------
    // Private validation steps
    // ----------------------------------------------------------------

    private void validatePasswordsMatch(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("VALID-002:Passwords do not match");
        }
    }

    private void validateInstitutionalEmail(RegisterRequest request) {
        if (!request.getEmail().toLowerCase().endsWith("@" + institutionalDomain)) {
            throw new IllegalArgumentException(
                "VALID-003:Email must be a CIT-U institutional email (@" + institutionalDomain + ")");
        }
    }

    private void validateNoDuplicateEmail(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("DB-002:An account with this email already exists");
        }
    }

    private void validateNoDuplicateStudentId(RegisterRequest request) {
        if (request.getStudentId() != null &&
                userRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalStateException(
                "DB-002:An account with this Student/Faculty ID already exists");
        }
    }
}