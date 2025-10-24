package com.maarco.repository;

import com.maarco.entities.NotificationEntity;
import com.maarco.entities.NotifySendSuccess;
import com.maarco.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing NotifySendSuccess entities in MongoDB.
 * Provides methods to track and query successful notification deliveries,
 * including operations to find, filter, and delete successful notification records.
 */
@Repository
public interface NotifySendSuccessRepository extends MongoRepository<NotifySendSuccess, Long> {

    /**
     * Finds all successful notification deliveries for a specific email address
     * @param email The email address to search for
     * @return List of successful notification records for the given email
     */
    List<NotifySendSuccess> findByUserEmail(String email);

    /**
     * Finds all successful deliveries associated with a specific notification
     * @param notificationEntity The notification entity to search for
     * @return List of successful delivery records for the given notification
     */
    List<NotifySendSuccess> findByNotificationEntity(NotificationEntity notificationEntity);

    /**
     * Deletes all successful notification records for a specific user
     * @param user The user whose successful notification records should be deleted
     */
    void deleteByUser(User user);

    /**
     * Finds a specific successful notification record by its ID and associated user
     * @param id The ID of the successful notification record
     * @param user The user associated with the record
     * @return Optional containing the matching record if found
     */
    Optional<NotifySendSuccess> findByIdAndUser(Long id, User user);
}