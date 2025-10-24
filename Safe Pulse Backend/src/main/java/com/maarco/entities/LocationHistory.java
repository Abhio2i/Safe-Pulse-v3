/**
 * The LocationHistory class represents a document in MongoDB that stores
 * location tracking data for users in the system.
 * <p>
 * This entity stores:
 * - Geographic coordinates (latitude/longitude)
 * - Timestamp of when the location was recorded
 * - Reference to the associated User
 * <p>
 * Key Features:
 * - Maps to "location_history" collection in MongoDB
 * - Uses custom deserializers for flexible date/time parsing
 * - Maintains relationship with User through DBRef
 * - Includes JSON formatting for timestamps
 * <p>
 * Used for tracking user movements/locations over time, which could be used for:
 * - Activity monitoring
 * - Location-based services
 * - Analytics of user movements
 */


package com.maarco.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.maarco.config.FlexibleLocalDateTimeDeserializer;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "location_history")
public class LocationHistory {

    @Id
    private String id;

    private Double latitude;
    private Double longitude;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime timestamp = LocalDateTime.now();
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
//    private LocalDateTime timestamp;

    @DBRef
    private User user;  // Reference to the user whose location is being stored
}
