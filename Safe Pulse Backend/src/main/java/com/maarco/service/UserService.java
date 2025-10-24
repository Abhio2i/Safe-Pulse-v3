package com.maarco.service;


import com.maarco.config.AppConstants;
import com.maarco.entities.LocationHistory;
import com.maarco.entities.RelationUser;
import com.maarco.entities.Role;
import com.maarco.entities.User;
import com.maarco.exception.RegistrationException;
import com.maarco.exception.UserAlreadyExistsException;
import com.maarco.registration.RegistrationRequest;
import com.maarco.registration.token.VerificationToken;
import com.maarco.registration.token.VerificationTokenRepository;
import com.maarco.repository.LocationHistoryRepository;
import com.maarco.repository.RelationUserRepository;
import com.maarco.repository.RoleRepo;
import com.maarco.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Core service for managing user operations including:
 * - User registration and authentication
 * - Profile management and updates
 * - Email verification handling
 * - Location history tracking
 * - User relationships management
 *
 * <p>Implements IUserService interface and handles all business logic
 * related to user accounts and their associated data.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final VerificationTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private LocationHistoryRepository locationHistoryRepository;
//    @Autowired
//    private AllToggleRepository allToggleRepository;

    public List<User> getUser() {
        return userRepository.findAll();
    }

    //    public Page<User> getAllUsers(Pageable pageable) {
//        return userRepository.findAll(pageable);
//    }
    public User saveUser(User user) {
        // Use your UserRepository to save the user entity
        return userRepository.save(user);
    }

    // for registering the user in application
    public User registerUser(RegistrationRequest request) {
        // Check if the user with the provided email already exists
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        // Check registration term condition
        if (!request.registrationTermCondition()) {
            throw new RegistrationException("Registration terms and conditions must be accepted.");
        }

        // Proceed with user registration
        User newUser = new User();
        newUser.setMobileNo(request.mobileNo());
        newUser.setUserName(request.userName());
        newUser.setEmail(request.email());
        newUser.setDeviceType(request.deviceType());
        newUser.setLatitude(request.latitude());
        newUser.setLongitude(request.longitude());
        newUser.setPassword(passwordEncoder.encode(request.password()));

        // Assign a role to the user (adjust as needed)
        Role role = roleRepo.findById(AppConstants.NORMAL_USER).orElseThrow();
        newUser.getRoles().add(role);

        // Save the user
        User savedUser = userRepository.save(newUser);


        return savedUser;
    }


    public User updateUserNotificationToken(String username, String newToken) {
        Optional<User> optionalUser = userRepository.findByEmail(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (newToken != null) {
                user.updateNotificationToken(newToken);
            }

            return userRepository.save(user);
        }

        // Handle the case when the user with the given username is not found
        return null;
    }

    // to check if e mail in use
    public boolean isEmailInUse(String email) {
        // Perform a database query to check if the email is already in use.
        Optional<User> existingUserOptional = userRepository.findByEmail(email);

        // Check if the Optional contains a user and return true if it does.
        return existingUserOptional.isPresent();
    }

    // to update the user
    public User updateUser(User user) {
        // This method should update the user and save the changes to the database
        return userRepository.save(user);
    }



    public User findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByEmail(username);
        return userOptional.orElse(null); // Return null if not found, or handle differently if needed
    }

    @Override
    public Optional<User> findByEmail(String email) {

        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUserVerificationToken(User theUser, String token) {
        // Check if the user already has a verification token
        VerificationToken existingToken = tokenRepository.findByUser(theUser);

        if (existingToken != null) {
            // If an existing token is found, update it with the new token and reset the expiration time
            existingToken.setToken(token);
            existingToken.setExpirationTime(existingToken.getTokenExpirationTime());
            tokenRepository.save(existingToken);
        } else {
            // If no existing token is found, create a new one
            var verificationToken = new VerificationToken(token, theUser);
            tokenRepository.save(verificationToken);
        }
    }


    @Override
    public String validateToken(String theToken) {
        VerificationToken token = tokenRepository.findByToken(theToken);
        if (token == null) {
            return "Invalid verification token";
        }
        User user = token.getUser();
        Calendar calendar = Calendar.getInstance();
        if ((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0) {
            tokenRepository.delete(token);
            return "Token already expired";
        }
        user.setEmailVerified(true);
        userRepository.save(user);
        return "valid";
    }


    @Autowired
    private RelationUserRepository relationUserRepository;

//    public User findByEmail(String email) {
//        return userRepository.findByEmail(email);
//    }

    public RelationUser findRelationUserById(String relationId) {
        return relationUserRepository.findById(relationId).orElse(null);
    }

    public void saveRelationUser(RelationUser relationUser) {
        relationUserRepository.save(relationUser);
    }


    public List<LocationHistory> getLocationHistoryByUser(User user) {
        return locationHistoryRepository.findByUser(user);
    }

//    public List<LocationHistory> getLocationHistoryByUserAndDatee(User user, LocalDate date) {
//        return locationHistoryRepository.findByUserAndDate(user, date);
//    }


//    public List<LocationHistory> getLocationHistoryByUserAndDate(User user, LocalDate date) {
//        LocalDateTime startOfDay = date.atStartOfDay();
//        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
//        return locationHistoryRepository.findByUserAndTimestampBetween(user, startOfDay, endOfDay);
//        return locationHistoryRepository.findByUserAndTimestampBetweenOrderByTimestampAsc(user, startOfDay, endOfDay);
//    }

    public List<LocationHistory> getLocationHistoryByUserAndDate(User user, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<LocationHistory> histories = locationHistoryRepository.findByUserAndTimestampBetween(user, startOfDay, endOfDay);
        histories.sort(Comparator.comparing(LocationHistory::getTimestamp));
        return histories;
    }

//    public List<LocationHistory> getLocationHistoryByUserAndTimeRange(User user, LocalDateTime startTime, LocalDateTime endTime) {
//        return locationHistoryRepository.findByUserAndTimestampBetweenOrderByTimestampAsc(user, startTime, endTime);
//    }

    // In your UserService implementation
    public List<LocationHistory> getLocationHistoryByUserAndTimeRange(User user, LocalDateTime start, LocalDateTime end) {
        return locationHistoryRepository.findByUserAndTimestampBetween(user, start, end);
    }
//    public boolean isUserLinked(String fromUserId, String toUserId) {
//        // Check if you have a linked relationship with the other user
//        Optional<RelationUser> relation = relationUserRepository.findLinkedRelationship(fromUserId, toUserId);
//        return relation.isPresent();
//    }

}
