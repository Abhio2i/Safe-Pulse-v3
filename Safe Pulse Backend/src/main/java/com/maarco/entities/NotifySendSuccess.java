/**
 * The NotifySendSuccess class represents a successful notification delivery record
 * stored in MongoDB. It tracks when and to whom a notification was successfully sent.
 * <p>
 * **Key Features:**
 * - Logs the date (localDate) and time (startTime) of successful notification delivery.
 * - Stores the notification content (body) that was sent.
 * - Maintains references to both the recipient (User) and the original notification (NotificationEntity).
 * <p>
 * **Purpose:**
 * - Audit trail for successful notifications.
 * - Analytics on notification delivery times.
 * - Linking successful deliveries to users and their notifications.
 * <p>
 * **Relationships:**
 * - @DBRef to User: Which user received this notification.
 * - @DBRef to NotificationEntity: Which notification template was used.
 */

package com.maarco.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class NotifySendSuccess {

    @Id
    private Long id;

    private LocalDate localDate = LocalDate.now();
    private LocalTime startTime;
    private String body;

    @DBRef
    private User user;

    @DBRef
    private NotificationEntity notificationEntity; // Reference to NotificationEntity


}
