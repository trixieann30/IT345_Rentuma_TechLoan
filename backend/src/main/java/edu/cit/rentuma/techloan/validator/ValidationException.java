package edu.cit.rentuma.techloan.validator;

/**
 * Custom exception for validation failures
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
