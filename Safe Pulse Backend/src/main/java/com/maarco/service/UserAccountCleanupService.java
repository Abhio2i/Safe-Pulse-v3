package com.maarco.service;

import com.maarco.entities.User;
import com.maarco.registration.token.VerificationTokenRepository;
import com.maarco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling OTP (One-Time Password) operations using Firebase Authentication.
 * Provides functionality to initiate OTP sending to user phone numbers by creating
 * temporary Firebase user records. The actual OTP generation and SMS delivery
 * is handled by Firebase's phone authentication system.
 */
@Service
public class UserAccountCleanupService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    // to delete the data from the database if user did not verify the mail in next 30 minutes
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupUnverifiedUsers() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(30);
        List<User> unverifiedUsers = userRepository.findByEmailVerifiedFalseAndRegistrationTimestampBefore(oneMinuteAgo);

        List<ArrayList> arrayLists = new ArrayList<>();
        if (!unverifiedUsers.isEmpty()) {
            for (User user : unverifiedUsers) {
                user.getRoles().clear();
                // Delete associated data as needed
                userRepository.delete(user);
            }
        }
    }


}
