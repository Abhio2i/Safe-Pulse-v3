/**
 * The UserProfile class represents extended profile information for users,
 * storing personal, physical, and social media details separately from
 * the core user authentication data.
 * <p>
 * **Key Features:**
 * - Comprehensive personal information (name, gender, DOB)
 * - Physical attributes (height, weight, BMI/BMR calculations)
 * - Work and lifestyle information
 * - Social media account linkages
 * - Maintains a reference to the parent User document
 * <p>
 * **Design Purpose:**
 * - Separates frequently accessed auth data (in User) from less-frequently
 * accessed profile data
 * - Enables efficient querying of user characteristics
 * - Supports health/fitness applications through body metrics
 */

package com.maarco.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.annotation.Collation;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Data

@NoArgsConstructor
@AllArgsConstructor
@Document
public class UserProfile {
    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String profileImg;



    @JsonIgnore
    @DBRef
    private User user;


}
