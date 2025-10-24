package com.maarco.registration;

/**
 * Represents a user registration request containing all necessary details for account creation.
 * This is an immutable record type containing:
 * - userName: The desired username
 * - mobileNo: User's mobile number
 * - email: User's email address
 * - password: User's chosen password
 * - deviceType: Type of device used for registration
 * - latitude: Geographical coordinate
 * - longitude: Geographical coordinate
 * - registrationTermCondition: Flag indicating acceptance of terms and conditions
 */
public record RegistrationRequest(
        String userName,
        String mobileNo,
        String email,
        String password,
        String deviceType,
        Double latitude,
        Double longitude,
        boolean registrationTermCondition
) {
}
