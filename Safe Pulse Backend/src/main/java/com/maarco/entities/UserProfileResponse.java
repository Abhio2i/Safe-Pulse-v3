/**
 * The UserProfileResponse class represents a simplified, serializable view
 * of user profile data designed specifically for API responses.
 * <p>
 * **Key Characteristics:**
 * - Data Transfer Object (DTO) pattern implementation
 * - Combines core profile information with selected user details
 * - Optimized for client consumption (no sensitive data or references)
 * - Contains calculated health metrics (BMI)
 * <p>
 * **Difference from UserProfile Entity:**
 * - Flattened structure (no DBRefs)
 * - Combines data from User and UserProfile entities
 * - Only includes client-needed fields
 * - Uses java.util.Date instead of LocalDate for wider compatibility
 */

package com.maarco.entities;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String gender;
    private Date dateOfBirth;
    private double height;
    private double weight;
    private double bmi;


    private String workLevel;
    private String occupation;


    public UserProfileResponse(String workLevel, String occupation) {
        this.workLevel = workLevel;
        this.occupation = occupation;
    }
}
