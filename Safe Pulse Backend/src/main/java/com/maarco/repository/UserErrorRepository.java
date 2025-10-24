package com.maarco.repository;

import com.maarco.entities.UserError;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing UserError entities in MongoDB.
 *
 * <p>Provides basic CRUD operations for tracking and managing user-related errors.
 * The repository uses Long as the ID type for UserError entities.</p>
 *
 * <p>Extends MongoRepository which provides standard methods including:
 * <ul>
 *   <li>save(UserError entity)</li>
 *   <li>findById(Long id)</li>
 *   <li>findAll()</li>
 *   <li>deleteById(Long id)</li>
 *   <li>count()</li>
 * </ul>
 * </p>
 *
 * <p>Custom query methods for specific error tracking requirements can be added as needed.</p>
 */
@Repository
public interface UserErrorRepository extends MongoRepository<UserError, Long> {
    // Custom query methods for specific error tracking can be added here
    // Example:
    // List<UserError> findByUserId(Long userId);
    // List<UserError> findByErrorCode(String errorCode);
    // List<UserError> findByTimestampBetween(Date start, Date end);
}