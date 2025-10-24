
/**
 * Custom unchecked exception for handling registration-related errors.
 * Indicates problems during user registration process.
 * <p>
 * Extends RuntimeException to avoid mandatory catching while
 * providing domain-specific error classification.
 */

package com.maarco.exception;

public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }
}
