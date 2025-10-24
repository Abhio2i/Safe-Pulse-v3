package com.maarco.service;


import com.maarco.entities.User;
import com.maarco.entities.UserProfile;
import com.maarco.exception.ResourceNotFoundException;
import com.maarco.exception.UserNotFoundException;
import com.maarco.repository.UserProfileRepository;
import com.maarco.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

/**
 * Service class for managing user profile operations including:
 * - Profile creation and updates
 * - BMI and BMR calculations
 * - Health metric tracking
 * - User profile data management
 *
 * <p>Handles all business logic related to user profiles including
 * health calculations and profile data persistence.</p>
 */
@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    // New method — to fetch UserProfile by userId
    public UserProfile findByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User Profile not found for user id: " + userId));
    }
//    public UserProfile saveOrUpdateVariableType(String username, String variableType) {
//        // Fetch the UserProfile based on the userId
//        Optional<UserProfile> userProfileOptional = Optional.ofNullable(userProfileRepository.findByUserEmail(username));
//
//        UserProfile userProfile = userProfileOptional.orElseThrow(() ->
//                new UserNotFoundException("User with email " + username + " not found"));
//
//        // Update the variableType
//        userProfile.setVariableType(variableType);
//
//        // Save and return the updated UserProfile
//        return userProfileRepository.save(userProfile);
//    }


    // to create the user profile
    public UserProfile createUserProfile(UserProfile userProfile, String userId) throws ParseException {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Profile"));


        // Set the user reference in the UserProfile
        userProfile.setUser(user);

        // Calculate and set the BMI
//        double heightInInches = userProfile.getHeightFt() * 12 + userProfile.getHeightIn();
//        double heightInDecimal = heightInInches / 12.0; // Convert total inches to feet in decimal format
//        System.out.println("height in decimal .........." + heightInDecimal);
//        double bmi = calculateBMI(userProfile.getGender(), userProfile.getHeightFt(), userProfile.getHeightIn(), userProfile.getWeight());
//        userProfile.setBmi(bmi);

//        userProfile = activityTypeService.updateActivityType(user.getUserId(), userProfile.getOccupation());

//        UserProfile userProfile = activityTypeService.updateActivityType(user.getUserId(), updateActivityTypeDTO.getOccupation());

        UserProfile newProfile = this.userProfileRepository.save(userProfile);
        return newProfile;

    }


    public double calculateBMI(String gender, int heightFt, int heightIn, double weight) {
        // Convert height to centimeters
        double heightInCM = (heightFt * 12 + heightIn) * 2.54; // 1 inch = 2.54 centimeters

        // Convert height from centimeters to meters for BMI calculation
        double heightInMeters = heightInCM / 100; // Convert centimeters to meters

        // Calculate BMI based on gender
        double bmi;
        if (gender.equalsIgnoreCase("male")) {
            bmi = weight / (heightInMeters * heightInMeters);
        } else if (gender.equalsIgnoreCase("female")) {
            // Adjusted calculation for females
            // bmi = (weight / (heightInMeters * heightInMeters)) * 1.07 - (148 * (weight / heightInMeters)) + 4.5;
            bmi = weight / (heightInMeters * heightInMeters);
        } else {
            // Default to a generic BMI calculation if gender is not specified or recognized
            bmi = weight / (heightInMeters * heightInMeters);
        }
        return bmi;
    }

    // Method to calculate BMR
//    public double calculateBMR(UserProfile userProfile) {
//        String gender = userProfile.getGender();
//        int age = calculateAgee(userProfile.getDateOfBirth());
////        double weight = userProfile.getWeight();
////        double heightInCM = convertToCentimeters(userProfile.getHeightFt(), userProfile.getHeightIn());
//
//        double bmr;
//
//        if (gender.equalsIgnoreCase("male")) {
//            if (age >= 18 && age <= 30) {
//                bmr = (15.1 * weight) + 692.2;
//            } else if (age > 30 && age <= 60) {
//                bmr = (11.5 * weight) + 873;
//            } else {
//                bmr = (11.7 * weight) + 587.7;
//            }
//            bmr *= 0.9; // Apply activity factor for males
//        } else if (gender.equalsIgnoreCase("female")) {
//            if (age >= 18 && age <= 30) {
//                bmr = (14.8 * weight) + 486.6;
//            } else if (age > 30 && age <= 60) {
//                bmr = (8.1 * weight) + 845.6;
//            } else {
//                bmr = (9.1 * weight) + 658.5;
//            }
//            bmr *= 0.91; // Apply activity factor for females
//        } else {
//            throw new IllegalArgumentException("Invalid gender specified");
//        }
//        System.out.println("output of bmr" + bmr);
//
//        return bmr;
//    }


    //update user profile
    public UserProfile saveUserProfile(UserProfile userProfile) {
        // This method should save or update the user's profile data in the database
        return userProfileRepository.save(userProfile);
    }

    public UserProfile findByUsername(String username) {
        // Implement the logic to fetch health trends by the user's username
        return userProfileRepository.findByUserEmail(username);
    }


    // to update the user profile
    public void updateUserProfile(Long id, UserProfile updatedProfile) {
        Optional<UserProfile> existingProfile = userProfileRepository.findById(id);
        if (existingProfile.isPresent()) {
            UserProfile userProfile = existingProfile.get();
            User user = userProfile.getUser();
            if (user != null) {
                user.setEmail(updatedProfile.getUser().getEmail());
                user.setMobileNo(updatedProfile.getUser().getMobileNo());
                // You can update other fields as well
                userProfileRepository.save(userProfile);
            } else {
                // Handle the case where the user is not associated with the UserProfile
                // Return an error response or take appropriate action
            }
        }
    }


    private int calculateAgee(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        return Period.between(dateOfBirth, currentDate).getYears();
    }


}

