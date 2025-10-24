
/**
 * Custom runtime exception indicating that a requested operation
 * exceeds allowed time duration limits.
 * <p>
 * This exception should be thrown when:
 * - A time-based constraint is violated
 * - Duration exceeds system/maximum allowed limits
 * - Scheduling requests go beyond permitted ranges
 * <p>
 * Inherits from RuntimeException (unchecked exception) since these
 * are typically validation failures that should be handled immediately.
 */

package com.maarco.exception;


public class ExceedsDurationLimitException extends RuntimeException {
    /**
     * Constructs exception with a descriptive error message
     * @param message Details about the duration violation
     */
    public ExceedsDurationLimitException(String message) {
        super(message);
    }
}
