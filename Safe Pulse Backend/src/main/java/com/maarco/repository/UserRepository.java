package com.maarco.repository;

import com.maarco.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User entities in MongoDB.
 * Provides methods for user search, filtering, and analytics including:
 * - Search by various criteria (name, email, phone)
 * - Filtering by connection status, verification status, and date ranges
 * - User counting and pagination
 */
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds all users by gender (case-insensitive)
     * @param gender The gender to search for (case insensitive)
     * @return List of users matching the specified gender
     */
    List<User> findAllByUserProfile_GenderIgnoreCase(String gender);

    /**
     * Searches users by query across multiple fields (username, email, mobile)
     * @param searchQuery The search term to match (case insensitive)
     * @param pageable Pagination information
     * @return Page of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(u.mobileNo) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<User> findBySearchQuery(String searchQuery, Pageable pageable);

    /**
     * Counts users matching search criteria across multiple fields
     * @param searchQuery The search term to match (case insensitive)
     * @return Count of users matching the search criteria
     */
    @Query("SELECT COUNT(u) FROM User u WHERE " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
            "LOWER(u.mobileNo) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    long countBySearchQuery(String searchQuery);

    /**
     * Finds a user by email address
     * @param email The email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds connected users who haven't been seen since a threshold time
     * @param threshold The cutoff datetime for last seen
     * @return List of inactive but still connected users
     */
    List<User> findByConnectedTrueAndLastSeenBefore(LocalDateTime threshold);

    /**
     * Finds users with unverified emails registered before a certain time
     * @param timestamp The cutoff datetime for registration
     * @return List of unverified users registered before the timestamp
     */
    List<User> findByEmailVerifiedFalseAndRegistrationTimestampBefore(LocalDateTime timestamp);

    /**
     * Counts users by gender through their profile
     * @param gender The gender to count
     * @return Count of users with the specified gender
     */
    Integer countByUserProfileGender(String gender);

    /**
     * Finds users registered between two dates
     * @param startDateTime Start date (inclusive)
     * @param endDateTime End date (inclusive)
     * @return List of users registered in the date range
     */
    List<User> findByLocalDateBetween(LocalDate startDateTime, LocalDate endDateTime);

    /**
     * Finds users registered within a datetime range
     * @param startOfMonth Start datetime (inclusive)
     * @param endOfMonth End datetime (inclusive)
     * @return List of users registered in the datetime range
     */
    List<User> findByRegistrationTimestampBetween(LocalDateTime startOfMonth, LocalDateTime endOfMonth);

    /**
     * Finds a user by their user ID
     * @param userId The unique user identifier
     * @return The user with matching ID
     */
    User findByUserId(String userId);

    /**
     * Finds all currently disconnected users
     * @return List of users with connected=false
     */
    List<User> findByConnectedFalse();

    @Query("SELECT u FROM User u JOIN RelationUser ru ON u.userId = ru.otherUserId WHERE ru.userId = :userId AND ru.status = 'CONNECTED'")
    List<User> findConnectedUsers(@Param("userId") String userId);
}