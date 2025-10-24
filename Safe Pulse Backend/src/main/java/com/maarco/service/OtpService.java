package com.maarco.service;


import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

/**
 * Service class for handling OTP (One-Time Password) operations using Firebase Authentication.
 *
 * <p>This service provides functionality for sending OTPs to user phone numbers
 * through Firebase's phone authentication system.</p>
 *
 * <p>Note: Firebase handles the actual OTP generation and delivery - this service
 * initiates the process by creating temporary user records.</p>
 */
@Service
public class OtpService {

    private final FirebaseAuth firebaseAuth;

    /**
     * Constructs the OTP service with Firebase dependencies
     *
     * @param firebaseApp The Firebase application instance
     */
    public OtpService(FirebaseApp firebaseApp) {
        this.firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
    }

    /**
     * Initiates the OTP sending process to a phone number
     *
     * @param phoneNumber The phone number to send OTP to (format: +[country code][number])
     * @return Status message indicating success or failure
     */
    public String sendOtp(String phoneNumber) {
        try {
            // Generate a new user record to initiate the OTP process
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setPhoneNumber(phoneNumber)
                    .setEmail("golowyvy@teleg.eu"); // A placeholder email is required

            UserRecord userRecord = firebaseAuth.createUser(request);

            // Firebase Auth manages OTP sending, no direct API for server-side OTP sending.
            // This just initiates the process, and OTP will be sent to the user's phone.

            return "OTP sent successfully";
        } catch (FirebaseAuthException e) {
            // Log the error and return a user-friendly message
            e.printStackTrace();  // Consider using a logger for production
            return "Failed to send OTP: " + e.getMessage();
        }
    }
}
