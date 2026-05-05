package edu.cit.rentuma.techloan.features.auth.validator;

import edu.cit.rentuma.techloan.features.auth.dto.RegisterRequest;
import org.springframework.stereotype.Component;

@Component("googleRegistrationValidator")
public class GoogleRegistrationValidator implements RegistrationValidator {

    @Override
    public void validate(RegisterRequest request) {
        // OAuth flow: Google already verified the user's identity.
    }
}
