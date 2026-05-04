package edu.cit.rentuma.techloan.features.auth.validator;

import edu.cit.rentuma.techloan.features.auth.dto.RegisterRequest;

public interface RegistrationValidator {
    void validate(RegisterRequest request);
}
