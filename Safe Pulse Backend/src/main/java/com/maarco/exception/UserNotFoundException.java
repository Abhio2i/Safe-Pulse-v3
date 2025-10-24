/**
 * Custom exception thrown when a requested user cannot be found in the system.
 * Typically occurs during login, profile lookup, or user management operations.
 */
package com.maarco.exception;

public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs the exception with username/identifier that wasn't found
     * @param identifier The username, email, or ID that couldn't locate a user
     */
    public UserNotFoundException(String identifier) {
        super("User not found with identifier: " + identifier);
    }

    /**
     * Enhanced constructor with lookup context
     * @param fieldName The field used for search (username/email/id)
     * @param value The value that was searched
     */
    public UserNotFoundException(String fieldName, String value) {
        super(String.format("User not found with %s: %s", fieldName, value));
    }
}