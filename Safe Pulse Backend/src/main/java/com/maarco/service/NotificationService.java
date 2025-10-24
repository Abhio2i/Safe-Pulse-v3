package com.maarco.service;


import com.maarco.controler.NotificationController;
import com.maarco.dtos.NotificationDetailsDTO;
import com.maarco.dtos.NotifySendSuccessDTO;
import com.maarco.entities.NotificationEntity;
import com.maarco.entities.NotifySendSuccess;
import com.maarco.entities.User;
import com.maarco.repository.NotificationRepository;
import com.maarco.repository.NotifySendSuccessRepository;
import com.maarco.repository.UserRepository;
//import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing notification operations including:
 * - Notification delivery and tracking
 * - User notification preferences management
 * - Disconnected user alerts
 * - Notification success logging
 *
 * <p>Integrates with Firebase Cloud Messaging for push notifications
 * and maintains audit logs of notification delivery status.</p>
 */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Autowired
    private NotifySendSuccessRepository notifySendSuccessRepository;


    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void toggleNotification(String username, String notificationType, boolean notificationOn) {
        List<NotificationEntity> userNotifications = notificationRepository.findByUserEmail(username);

        for (NotificationEntity notification : userNotifications) {
            // Check if the notification has the target type
            if (notification.hasNotificationType(notificationType)) {
                notification.setNotificationOn(notificationOn);
                notificationRepository.save(notification);
            }
        }
    }


    public void deleteNotification(User user, Long notificationId) {
        try {
            Optional<NotificationEntity> notificationOptional = notificationRepository.findById(notificationId);

            if (notificationOptional.isPresent()) {
                NotificationEntity notification = notificationOptional.get();

                // Remove associated NotifySendSuccess records
                List<NotifySendSuccess> notifySendSuccessList = notifySendSuccessRepository.findByNotificationEntity(notification);
                notifySendSuccessList.forEach(notifySendSuccess -> notifySendSuccess.setUser(null)); // Or handle deletion/update as per your application logic
                notifySendSuccessRepository.deleteAll(notifySendSuccessList);

                // Remove the notification from the user's collection
                user.getNotifications().remove(notification);

                notificationRepository.delete(notification);
                log.info("Notification deleted successfully: {}", notificationId);
            } else {
                log.error("Notification with ID {} not found", notificationId);
                throw new RuntimeException("Notification not found");
            }
        } catch (Exception e) {
            log.error("Failed to delete notification with ID {}", notificationId, e);
            throw new RuntimeException("Failed to delete notification", e);
        }
    }


    private void saveNotificationSendSuccess(NotificationEntity notification) {
        if (notification == null || notification.getUser() == null) {
            log.warn("Invalid NotificationEntity or User. Skipping NotifySendSuccess save.");
            return;
        }

        NotifySendSuccess successDetails = new NotifySendSuccess();
        successDetails.setUser(notification.getUser());

        LocalTime startTime = notification.getStartTime();

        if (startTime != null) {
            successDetails.setStartTime(startTime);
        } else {
            log.warn("NotificationEntity with null startTime encountered while saving NotifySendSuccess.");
            successDetails.setStartTime(LocalTime.now());
        }

        // Set the id of the associated NotificationEntity
        successDetails.setNotificationEntity(notification);

        // Set the notification type in the body field
        successDetails.setBody(notification.getNotificationType());

        // Save success details in the database
        notifySendSuccessRepository.save(successDetails);
    }


    public boolean deleteByIdAndUser(Long id, User user) {
        Optional<NotifySendSuccess> record = notifySendSuccessRepository.findByIdAndUser(id, user);
        if (record.isPresent()) {
            notifySendSuccessRepository.delete(record.get());
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public void deleteAllByUser(User user) {
        notifySendSuccessRepository.deleteByUser(user);
    }


//For O2i Secure

    //    @Scheduled(fixedRate = 10000) // Run every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000) // every 15 minutes
    public void checkDisconnectedUsers() {
        notifyDisconnectedUsers();
    }

    // Method to notify disconnected users
    public String notifyDisconnectedUsers() {
        // Find all users where connected = false
        List<User> disconnectedUsers = userRepository.findByConnectedFalse();

        int notificationCount = 0;

        for (User user : disconnectedUsers) {
            // Check if user has a notification token
            if (user.getNotificationToken() != null && !user.getNotificationToken().isEmpty()) {
                NotificationEntity notification = new NotificationEntity();
                notification.setRecipientToken(user.getNotificationToken());
                notification.setTitle("Connection Status");

//                // Format the lastSeen time if available
//                String lastSeenTime = (user.getLastSeen() != null)
//                        ? user.getLastSeen().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
//                        : "unknown time";
//
////                notification.setBody("User is disconnected");
//                notification.setBody("User is disconnected since " + lastSeenTime);
                // Format the lastSeen time if available
                String lastSeenTime = (user.getLastSeen() != null)
                        ? user.getLastSeen().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                        : "unknown time";

// Create a properly formatted notification message
                String notificationBody = "🔴 " + user.getUserName() + " is offline\n" +
                        "⌚ Last active: " + lastSeenTime;

                notification.setBody(notificationBody);

                // Send notification
                firebaseMessagingService.sendNotificationByToken(notification);
                notificationCount++;
            }
        }

        return "Sent disconnection notifications to " + notificationCount + " users";
    }
}





