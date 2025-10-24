
/**
 * The UserError class represents error logs associated with user operations in the system.
 * It tracks and records errors that occur during user-related processes for debugging and monitoring purposes.
 * <p>
 * **Key Features:**
 * - Records error messages with timestamps
 * - Links each error to the specific user who encountered it
 * - Provides audit trail for troubleshooting user-related issues
 * <p>
 * **Common Use Cases:**
 * - Logging authentication failures
 * - Tracking form submission errors
 * - Recording system exceptions related to user actions
 * - Monitoring user experience problems
 */
package com.maarco.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class UserError {

    @Id
    private Long errorId;
    private String error;
    private Timestamp timestamp;

    @DBRef
    private User user;


}
