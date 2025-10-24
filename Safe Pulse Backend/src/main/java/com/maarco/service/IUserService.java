package com.maarco.service;

import com.maarco.entities.User;
import com.maarco.registration.RegistrationRequest;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for user management operations.
 * Defines core user-related business logic including:
 * - User registration and retrieval
 * - Email verification handling
 * - User lookup functionality
 */
public interface IUserService {

    /**
     * Retrieves all users from the system
     * @return List of all users
     */
    List<User> getUser();

    /**
     * Registers a new user in the system
     * @param request Registration details including email, password etc.
     * @return The newly registered user entity
     */
    User registerUser(RegistrationRequest request);

    /**
     * Finds a user by their email address
     * @param email The email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Associates a verification token with a user for email confirmation
     * @param theUser The user to associate the token with
     * @param verificationToken The verification token string
     */
    void saveUserVerificationToken(User theUser, String verificationToken);

    /**
     * Validates a user's verification token
     * @param theToken The token to validate
     * @return Status message indicating validation result
     */
    String validateToken(String theToken);
}