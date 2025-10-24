package com.maarco.repository;

import com.maarco.entities.User;
import com.maarco.entities.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing UserProfile entities in MongoDB.
 * Provides methods to query and manage user profile information including:
 * - Finding profiles by user, gender, or email
 * - Counting profiles by gender
 * - Accessing profile information by user ID
 */
public interface UserProfileRepository extends MongoRepository<UserProfile, Long> {

    /**
     * Finds all profiles associated with a specific user
     * @param user The user entity to search for
     * @return List of profiles belonging to the user
     */
    List<UserProfile> findAllByUser(User user);

    /**
     * Finds profiles by gender
     * @param user The gender to search for (parameter name should likely be 'gender')
     * @return List of profiles matching the specified gender
     */
    List<UserProfile> findByGender(String user);

    /**
     * Finds a single profile by user email
     * @param email The email address to search for
     * @return The user profile matching the email, or null if not found
     */
    UserProfile findByUserEmail(String email);

    /**
     * Finds all user profiles by gender
     * @param gender The gender to filter by
     * @return List of profiles matching the specified gender
     */
    List<UserProfile> findAllUserByGender(String gender);

    /**
     * Counts the number of profiles with a specific gender
     * @param gender The gender to count
     * @return The count of profiles with the specified gender
     */
    Integer countByGender(String gender);

    /**
     * Finds a user profile by user ID
     * @param id The user ID to search for
     * @return The user profile associated with the user ID
     */
    UserProfile findByUserUserId(Long id);
    Optional<UserProfile> findByUser(User user);
    Optional<UserProfile> findByUser_UserId(String userId);


}