package com.maarco.repository;

/**
 * Repository interface for managing NotificationEntity in MongoDB.
 * Provides custom query methods for notification-related operations including:
 * - Finding notifications by user or email
 * - Deleting notifications by ID and user ID
 * - Querying notifications by time ranges
 * - Finding specific notification types for users
 */

import com.maarco.entities.NotificationEntity;
import com.maarco.entities.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUser(User user);

    List<NotificationEntity> findByUserEmail(String email);

    void deleteByIdAndUser_UserId(Long id, Long userId);

    @Query("SELECT n FROM NotificationEntity n WHERE n.startTime = :startTime")
    List<NotificationEntity> findByStartTime(@Param("startTime") LocalTime startTime);

    List<NotificationEntity> findByStartTimeBetween(LocalTime startTime, LocalTime endTime);

    NotificationEntity findByUserAndNotificationType(User user, String notificationType);


//    Optional<NotificationEntity> findByUserAndNotificationType(String username, String notificationType);

}