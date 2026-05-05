package edu.cit.rentuma.techloan.features.auth.validator;

import edu.cit.rentuma.techloan.features.auth.dto.RegisterRequest;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
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
