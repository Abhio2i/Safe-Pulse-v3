package com.maarco.service;


import com.maarco.entities.NotificationEntity;
import com.maarco.entities.User;
import com.maarco.repository.NotificationRepository;
import com.maarco.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for handling Firebase Cloud Messaging (FCM) operations.
 * Provides functionality to send push notifications to individual devices
 * or broadcast to all registered users.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Sending targeted notifications using device tokens</li>
 *   <li>Broadcasting notifications to all registered users</li>
 *   <li>Integration with Firebase Admin SDK</li>
 *   <li>Error handling for notification failures</li>
 * </ul>
 */
@Service
public class FirebaseMessagingService {

    @Autowired
    private FirebaseMessaging firebaseMessaging;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * Sends a push notification to a specific device using its FCM token
     *
     * @param notificationEntity Contains notification details and recipient token
     * @return Status message indicating success or failure
     */
    public String sendNotificationByToken(NotificationEntity notificationEntity) {
        Notification notification = Notification.builder().setTitle(notificationEntity.getTitle()).setBody(notificationEntity.getBody())
//                .setImage(notificationEntity.getImage())
                .build();

        Message message = Message.builder().setToken(notificationEntity.getRecipientToken()).setNotification(notification).build();

        try {
            firebaseMessaging.send(message);
            return "Success Sending Notification";

        } catch (FirebaseMessagingException ex) {
            ex.printStackTrace();
            return "Error Sending Notification";
        }

    }

    /**
     * Broadcasts a notification to all registered users in the system
     *
     * @param notificationEntity Contains notification details (title, body)
     * @return Status message indicating completion
     */
    public String sendNotificationToAllUsers(NotificationEntity notificationEntity) {
        List<User> users = userRepository.findAll(); // Retrieve all users from the database

        for (User user : users) {
            NotificationEntity userNotification = new NotificationEntity();
            userNotification.setRecipientToken(user.getNotificationToken());
            // Set other notification details (title, body, etc.)
            userNotification.setTitle(notificationEntity.getTitle());
            userNotification.setBody(notificationEntity.getBody());
            // Send notification to each user
            sendNotificationByToken(userNotification);
        }

        return "Notifications sent to all users";
    }
}
