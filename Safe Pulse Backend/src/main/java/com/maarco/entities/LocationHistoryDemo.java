/**
 * The LocationHistoryDemo class is a MongoDB entity that stores user location history data
 * for demonstration or testing purposes.
 * <p>
 * This is similar to LocationHistory but mapped to a different collection ("location_history_demo"),
 * likely used for:
 * - Development testing
 * - Demo scenarios
 * - Temporary location data storage
 * <p>
 * Features:
 * - Stores GPS coordinates (latitude, longitude)
 * - Tracks when the location was recorded (timestamp)
 * - References the associated User via DBRef
 * - Uses flexible datetime parsing for the timestamp
 * - Formats timestamp output in "yyyy-MM-dd HH:mm:ss" (IST timezone)
 * <p>
 * Note: This appears to be a demo/test version of LocationHistory with identical fields
 * but stored separately to avoid mixing real and test data.
 */

package com.maarco.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Document(collection = "location_history_demo")
public class LocationHistoryDemo {

    @Id
    private String id;

    private Double latitude;
    private Double longitude;
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
//    private LocalDateTime timestamp = LocalDateTime.now();
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;

    @DBRef
    private User user;  // Reference to the user whose location is being stored
}
