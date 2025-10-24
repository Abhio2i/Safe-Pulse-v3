/**
 * Global exception handler for registration-related exceptions.
 * Provides centralized error handling for registration workflow.
 * <p>
 * Annotated with @RestControllerAdvice to handle exceptions across all controllers.
 */

package com.maarco.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RegistrationExceptionHandler {
    /**
     * Handles validation failures for method arguments
     * Returns field-level error messages
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    /**
     * Handles duplicate user registration attempts
     * Returns a simple error message
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public Map<String, String> userNotFound(UserAlreadyExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return error;
    }
}
