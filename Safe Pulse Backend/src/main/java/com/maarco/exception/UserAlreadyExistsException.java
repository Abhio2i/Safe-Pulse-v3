/**
 * Custom exception thrown when attempting to register or create a user
 * that already exists in the system (typically by email or username).
 *
 * This is a business logic exception that should result in an HTTP 409 Conflict
 * response as it indicates a duplicate resource creation attempt.
 */
package com.maarco.exception;

public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs the exception with a descriptive message
     * @param message Should indicate which field caused the conflict (email/username)
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Convenience constructor with field-specific details
     * @param fieldName The duplicate field (email/username/phone)
     * @param value The duplicate value that already exists
     */
    public UserAlreadyExistsException(String fieldName, String value) {
        super(String.format("User with %s '%s' already exists", fieldName, value));
    }
}