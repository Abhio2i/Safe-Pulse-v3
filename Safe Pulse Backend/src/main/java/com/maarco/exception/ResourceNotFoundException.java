/**
 * Custom exception for cases when a requested resource cannot be found.
 * Provides structured error information including resource type and identification details.
 */
package com.maarco.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceNotFoundException extends RuntimeException {

    private String resourceName;  // The type of resource not found (e.g., "User")
    private String fieldName;     // The field used for lookup (e.g., "email")
    private Object fieldValue;    // The value that failed to locate resource

    /**
     * Constructor for basic resource not found cases
     * @param resourceName The type of resource that wasn't found
     */
    public ResourceNotFoundException(String resourceName) {
        super(String.format("%s not found", resourceName));
        this.resourceName = resourceName;
    }

    /**
     * Detailed constructor with lookup context
     * @param resourceName Type of resource (e.g., "User")
     * @param fieldName Field used for search (e.g., "id")
     * @param fieldValue The searched value that returned no results
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}