/**
 * The NotificationEntity class represents a notification document stored in MongoDB.
 * It is used to manage and track notifications sent to users, including their type, timing, and status.
 * <p>
 * **Key Features:**
 * - Stores notification details like title, body, and recipient token (likely for push notifications).
 * - Tracks timing information (startTime and lastTime) for notification scheduling.
 * - Supports different notification types (notificationType) for categorization.
 * - Maintains an on/off toggle (notificationOn) to enable/disable notifications.
 * - References the associated User via @DBRef for user-specific notifications.
 * - Uses JSON identity handling (@JsonIdentityInfo) to prevent circular references during serialization.
 * <p>
 * **Common Use Cases:**
 * - Push notifications for mobile/web apps.
 * - Scheduled or recurring alerts.
 * - User-specific notification management.
 * <p>
 * **Example:**
 * - A "Disconnect" alert sent to a user when their device goes offline.
 * - A reminder notification triggered at a specific time.
 */

package com.maarco.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")

public class NotificationEntity {
    @Id
    private Long id;

    private String recipientToken;
    private String title = "Disconnect";
    private String body = "Disconnect";
    private LocalTime startTime;
    private LocalTime lastTime;
    private String notificationType;
    private boolean notificationOn = true;

    // Add a new method to check if the notification type matches
    public boolean hasNotificationType(String targetType) {
        return notificationType != null && notificationType.equals(targetType);
    }

    @DBRef
    private User user;

}