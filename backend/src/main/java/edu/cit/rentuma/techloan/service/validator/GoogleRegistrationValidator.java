package edu.cit.rentuma.techloan.service.validator;

import edu.cit.rentuma.techloan.dto.RegisterRequest;
import org.springframework.stereotype.Component;

/**
 * Refactoring 2 – Strategy Pattern (Behavioral):
 * Validation strategy for Google OAuth registrations.
 *
 * Google accounts are already verified by Google, so:
 *  - Institutional email domain check is skipped (Google provides the email).
 *  - Password match check is skipped (no password for OAuth users).
 *
 * This class exists purely to demonstrate how the Strategy pattern
 * allows new registration types without modifying AuthService.
 */
@Component("googleRegistrationValidator")
public class GoogleRegistrationValidator implements RegistrationValidator {

    @Override
    public void validate(RegisterRequest request) {
        // OAuth flow: Google already verified the user's identity.
        // No password or institutional domain validation required.
        // Add any OAuth-specific checks here (e.g., role whitelist) if needed.
    }
}