package com.maarco.controler;

import com.maarco.dtos.LinkedUserLocationHistoryDTO;
import com.maarco.dtos.LocationHistoryDTO;
import com.maarco.dtos.RelationUserDTO;
import com.maarco.emergencyContacts.MyContactDto;
import com.maarco.entities.LocationHistory;
import com.maarco.entities.RelationUser;
import com.maarco.entities.User;
import com.maarco.entities.UserProfile;
import com.maarco.repository.RelationUserRepository;
import com.maarco.repository.UserProfileRepository;
import com.maarco.repository.UserRepository;
import com.maarco.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/relationships")
public class RelationshipController {

    @Autowired
    private UserService userService;


public boolean existsRelationUserByFromUserAndToUserAndRelationName(User fromUser, User toUser, String relationName) {
    return relationUserRepository.existsByFromUserAndToUserAndRelationName(fromUser, toUser, relationName);
}

    @GetMapping("/get-all-my-contact/{userId}")
    public List<MyContactDto> getEmergencyContacts(@PathVariable String userId) {
        // Find all relations where the specified user is the toUser
        List<RelationUser> relations = relationUserRepository.findByToUserUserId(userId);

        return relations.stream()
                .map(relation -> {
                    MyContactDto dto = new MyContactDto();
                    User fromUser = relation.getFromUser();
                    UserProfile profile = fromUser.getUserProfile();

                    // Set name from user profile if available, otherwise from username
                    if (profile != null && profile.getFirstName() != null && profile.getLastName() != null) {
                        dto.setName(profile.getFirstName() + " " + profile.getLastName());
                    } else {
                        dto.setName(fromUser.getUserName());
                    }

                    dto.setRelation(relation.getRelationName());
                    dto.setNumber(fromUser.getMobileNo());
//                    dto.setImageUrl(fromUser.getUserProfile().getProfileImg());
                    dto.setImageUrl(
                            (fromUser.getUserProfile() != null && fromUser.getUserProfile().getProfileImg() != null)
                                    ? fromUser.getUserProfile().getProfileImg()
                                    : "/images/default-user-img.jpg"
                    );


                    return dto;
                })
                .collect(Collectors.toList());
    }


    @PostMapping("/sendRequest")
    public String sendRelationshipRequest(@RequestParam String fromEmail, @RequestParam String toEmail, @RequestParam String relationName) {
        Optional<User> fromUserOptional = userService.findByEmail(fromEmail);
        Optional<User> toUserOptional = userService.findByEmail(toEmail);

        if (fromUserOptional.isPresent() && toUserOptional.isPresent()) {
            User fromUser = fromUserOptional.get();
            User toUser = toUserOptional.get();

            // Check if a relationship request already exists
            boolean requestExists = existsRelationUserByFromUserAndToUserAndRelationName(fromUser, toUser, relationName);

            if (requestExists) {
                return "Relationship request already sent!";
            } else {
                RelationUser relationUser = new RelationUser();
                relationUser.setFromUser(fromUser);
                relationUser.setToUser(toUser);
                relationUser.setRelationName(relationName);
                relationUser.setIsLinked(0.0); // 0.0 means request is not accepted yet

                userService.saveRelationUser(relationUser);
                return "Relationship request sent successfully!";
            }
        } else {
            return "User not found!";
        }
    }

    public List<RelationUser> getRequestsForUser(String email) {
        Optional<User> userOptional = userService.findByEmail(email);
        return userOptional.map(relationUserRepository::findByToUser).orElse(List.of());
    }

//@GetMapping("/getUserRelations")
//public List<RelationUserDTO> getUserRelations(@RequestParam String email) {
//    Optional<User> userOptional = userService.findByEmail(email);
////    userOptional.get().getUserId();
//    if (userOptional.isEmpty()) {
//        return List.of();
//    }
//
//    User user = userOptional.get();
//    // Find all relationships where user is involved (both pending and accepted)
//    List<RelationUser> relations = relationUserRepository.findByFromUserOrToUser(user, user);
//
//    return relations.stream()
//            .map(relation -> {
//                // Determine the other user's email
//                String otherUserEmail = relation.getFromUser().getEmail().equals(email)
//                        ? relation.getToUser().getEmail()
//                        : relation.getFromUser().getEmail();
//
//                String userRelationId = relation.getFromUser().getUserId().equals(userOptional.get().getUserId())
//                        ? relation.getToUser().getUserId()
//                        : relation.getFromUser().getUserId();
//
//                // Determine relationship status
//                String relationStatus;
//                if (relation.getIsLinked() == 1.0) {
//                    relationStatus = "connected"; // accepted relationship
//                } else if (relation.getFromUser().getEmail().equals(email)) {
//                    relationStatus = "outgoing"; // request sent by current user
//                } else {
//                    relationStatus = "incoming"; // request received by current user
//                }
//
//                return new RelationUserDTO(
//                        relation.getRelationId(),
//                        relation.getIsLinked(),
//                        relation.getRelationName(),
//                        otherUserEmail,
//                        userRelationId,
//                        relationStatus
//                );
//            })
//            .collect(Collectors.toList());
//}
@Autowired
private UserProfileRepository userProfileRepository;

    @GetMapping("/getUserRelations")
    public List<RelationUserDTO> getUserRelations(@RequestParam String email) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            return List.of();
        }

        User user = userOptional.get();
        List<RelationUser> relations = relationUserRepository.findByFromUserOrToUser(user, user);

        return relations.stream()
                .map(relation -> {
                    // Determine the other user's email
                    String otherUserEmail = relation.getFromUser().getEmail().equals(email)
                            ? relation.getToUser().getEmail()
                            : relation.getFromUser().getEmail();

                    String userRelationId = relation.getFromUser().getUserId().equals(userOptional.get().getUserId())
                            ? relation.getToUser().getUserId()
                            : relation.getFromUser().getUserId();

                    String imageUrl = userProfileRepository.findByUser_UserId(userRelationId)
                            .map(UserProfile::getProfileImg)
                            .filter(img -> img != null && !img.isBlank())
                            .orElse("/images/default-user-img.jpg"); // your default image URL


                    // Determine relationship status
                    String relationStatus;
                    if (relation.getIsLinked() == 1.0) {
                        relationStatus = "connected"; // accepted relationship
                    } else if (relation.getFromUser().getEmail().equals(email)) {
                        relationStatus = "outgoing"; // request sent by current user
                    } else {
                        relationStatus = "incoming"; // request received by current user
                    }

                    // Determine activity status
                    String activityStatus = relation.getFromUser().getEmail().equals(email)
                            ? "Not Active"  // current user sent the request
                            : "Active";     // current user received the request

                    return new RelationUserDTO(
                            relation.getRelationId(),
                            relation.getIsLinked(),
                            relation.getRelationName(),
                            otherUserEmail,
                            userRelationId,
                            relationStatus,
                            activityStatus,
                            imageUrl
                    );
                })
                .collect(Collectors.toList());
    }

    @Autowired
private UserRepository UserRepository;

    @Autowired
    private RelationUserRepository relationUserRepository;
    public List<RelationUser> findRelationsByEmail(String email) {
        Optional<User> userOptional = UserRepository.findByEmail(email);
        return userOptional.map(user -> relationUserRepository.findByFromUserOrToUser(user, user)).orElse(null);
    }



    @PostMapping("/acceptRequest")
    public String acceptRelationshipRequest(@RequestParam String relationId) {
        RelationUser relationUser = userService.findRelationUserById(relationId);

        if (relationUser != null) {
            relationUser.setIsLinked(1.0); // 1.0 means request is accepted
            userService.saveRelationUser(relationUser);
            return "Relationship request accepted successfully!";
        } else {
            return "Relationship request not found!";
        }
    }



//    @GetMapping("/get-data-without-filter")
//    public List<LocationHistory> getToUserLocationHistory(@RequestParam String fromEmail, @RequestParam String toEmail) {
//        Optional<User> fromUserOptional = userService.findByEmail(fromEmail);
//        Optional<User> toUserOptional = userService.findByEmail(toEmail);
//
//        System.out.printf("+===================="+  fromUserOptional);
//
//        if (fromUserOptional.isPresent() && toUserOptional.isPresent()) {
//            User fromUser = fromUserOptional.get();
//            User toUser = toUserOptional.get();
//
//            // Find the relationship between fromUser and toUser
//            Optional<RelationUser> relationUserOptional = relationUserRepository.findByFromUserAndToUser(fromUser, toUser);
//
//            if (relationUserOptional.isPresent()) {
//                RelationUser relationUser = relationUserOptional.get();
//
//                // Check if the relationship is linked (isLinked = 1.0)
//                if (relationUser.getIsLinked() == 1.0) {
//                    // Fetch the location history of the toUser
//                    return userService.getLocationHistoryByUser(toUser);
//                } else {
//                    throw new RuntimeException("Relationship not linked!");
//                }
//            } else {
//                throw new RuntimeException("Relationship not found!");
//            }
//        } else {
//            throw new RuntimeException("User not found!");
//        }
//    }

    @GetMapping("/get-data-without-filter")
    public List<LocationHistoryDTO> getToUserLocationHistoryt(@RequestParam String fromEmail,
                                                             @RequestParam String toEmail,
                                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {

        Optional<User> fromUserOptional = userService.findByEmail(fromEmail);
        Optional<User> toUserOptional = userService.findByEmail(toEmail);

        System.out.println("dfasdhfjsac from user------"+fromUserOptional.get().getUserId());
        System.out.println("dfasdhfjsac to user------"+toUserOptional.get().getUserId());

        if (fromUserOptional.isPresent() && toUserOptional.isPresent()) {
            User fromUser = fromUserOptional.get();
            User toUser = toUserOptional.get();

            // Find the relationship between fromUser and toUser
            Optional<RelationUser> relationUserOptional = relationUserRepository.findByFromUserAndToUser(fromUser, toUser);

            if (relationUserOptional.isPresent()) {
                RelationUser relationUser = relationUserOptional.get();

                // Check if the relationship is linked (isLinked = 1.0)
                if (relationUser.getIsLinked() == 1.0) {
                    // Fetch the location history of the toUser
//                    List<LocationHistory> locationHistories = userService.getLocationHistoryByUser(toUser);
                    List<LocationHistory> locationHistories = userService.getLocationHistoryByUserAndDate(toUser, date);

                    // Map LocationHistory entities to LocationHistoryDTO
                    return locationHistories.stream()
                            .map(location -> new LocationHistoryDTO(
                                    location.getId(),
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    location.getTimestamp()
                            ))
                            .collect(Collectors.toList());
                } else {
                    throw new RuntimeException("Relationship not linked!");
                }
            } else {
                throw new RuntimeException("Relationship not found!");
            }
        } else {
            throw new RuntimeException("User not found!");
        }
    }


@GetMapping("/get-data-by-time-range")
public List<LocationHistoryDTO> getLocationHistoryByTimeRange(
        @RequestParam String fromEmail,
        @RequestParam String toEmail,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
        @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime
) {
    Optional<User> fromUserOptional = userService.findByEmail(fromEmail);
    Optional<User> toUserOptional = userService.findByEmail(toEmail);

    if (fromUserOptional.isPresent() && toUserOptional.isPresent()) {
        User fromUser = fromUserOptional.get();
        User toUser = toUserOptional.get();

        Optional<RelationUser> relationUserOptional = relationUserRepository.findByFromUserAndToUser(fromUser, toUser);

        if (relationUserOptional.isPresent()) {
            RelationUser relationUser = relationUserOptional.get();

            if (relationUser.getIsLinked() == 1.0) {
                // Combine date with time to create LocalDateTime
                LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
                LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

                // Validate time range
                if (startDateTime.isAfter(endDateTime)) {
                    throw new IllegalArgumentException("Start time must be before end time");
                }

                List<LocationHistory> locationHistories = userService.getLocationHistoryByUserAndTimeRange(toUser, startDateTime, endDateTime);

                return locationHistories.stream()
                        .map(location -> new LocationHistoryDTO(
                                location.getId(),
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getTimestamp()
                        ))
                        .collect(Collectors.toList());
            } else {
                throw new RuntimeException("Relationship not linked!");
            }
        } else {
            throw new RuntimeException("Relationship not found!");
        }
    } else {
        throw new RuntimeException("User not found!");
    }
}





    @GetMapping("/get-data-by-time-range-multiple-user")
    public Map<String, List<LocationHistoryDTO>> getLocationHistoryByTimeRange(
            @RequestParam String fromEmail,
            @RequestParam List<String> toEmails,  // Now accepts multiple emails
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime
    ) {
        Optional<User> fromUserOptional = userService.findByEmail(fromEmail);

        if (fromUserOptional.isEmpty()) {
            throw new RuntimeException("From user not found!");
        }

        User fromUser = fromUserOptional.get();
        Map<String, List<LocationHistoryDTO>> result = new HashMap<>();

        // Combine date with time to create LocalDateTime
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        // Validate time range
        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        for (String toEmail : toEmails) {
            Optional<User> toUserOptional = userService.findByEmail(toEmail);

            if (toUserOptional.isEmpty()) {
                result.put(toEmail, Collections.emptyList());
                continue;
            }

            User toUser = toUserOptional.get();
            Optional<RelationUser> relationUserOptional = relationUserRepository.findByFromUserAndToUser(fromUser, toUser);

            if (relationUserOptional.isEmpty()) {
                result.put(toEmail, Collections.emptyList());
                continue;
            }

            RelationUser relationUser = relationUserOptional.get();

            if (relationUser.getIsLinked() != 1.0) {
                result.put(toEmail, Collections.emptyList());
                continue;
            }

            List<LocationHistory> locationHistories = userService.getLocationHistoryByUserAndTimeRange(toUser, startDateTime, endDateTime);

            List<LocationHistoryDTO> dtos = locationHistories.stream()
                    .map(location -> new LocationHistoryDTO(
                            location.getId(),
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getTimestamp()
                    ))
                    .collect(Collectors.toList());

            result.put(toEmail, dtos);
        }

        return result;
    }
private static final Logger log = LoggerFactory.getLogger(RelationshipController.class);

//    @GetMapping("/getToUserLocationHistory")
//    public List<LocationHistoryDTO> getToUserLocationHistory(
//            @RequestParam String fromEmail,
//            @RequestParam String toEmail,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//
//        // Validate users and relationship
//        User fromUser = userService.findByEmail(fromEmail)
//                .orElseThrow(() -> new RuntimeException("From User not found!"));
//        User toUser = userService.findByEmail(toEmail)
//                .orElseThrow(() -> new RuntimeException("To User not found!"));
//
//        RelationUser relationUser = relationUserRepository.findByFromUserAndToUser(fromUser, toUser)
//                .orElseThrow(() -> new RuntimeException("Relationship not found!"));
//
//        if (relationUser.getIsLinked() != 1.0) {
//            throw new RuntimeException("Relationship not linked!");
//        }
//
//        // Get location history
//        List<LocationHistory> locationHistories = userService.getLocationHistoryByUserAndDate(toUser, date);
//
//        // Debug: Print raw data
//        System.out.println("Total records from DB: " + locationHistories.size());
//        if (!locationHistories.isEmpty()) {
//            System.out.println("First record time: " + locationHistories.get(0).getTimestamp());
//            System.out.println("Last record time: " + locationHistories.get(locationHistories.size()-1).getTimestamp());
//        }
//
//        // Sort by timestamp
//        locationHistories.sort(Comparator.comparing(LocationHistory::getTimestamp));
//
//        // New approach for filtering
//        List<LocationHistory> filteredHistories = new ArrayList<>();
//        if (!locationHistories.isEmpty()) {
//            // Add first record
//            filteredHistories.add(locationHistories.get(0));
//
//            // For subsequent records, add if either:
//            // 1. Location has changed significantly (optional)
//            // 2. Or 5 seconds have passed
//            for (int i = 1; i < locationHistories.size(); i++) {
//                LocationHistory current = locationHistories.get(i);
//                LocationHistory lastAdded = filteredHistories.get(filteredHistories.size()-1);
//
//                // Check if 5 seconds have passed OR location has changed
//                if (Duration.between(lastAdded.getTimestamp(), current.getTimestamp()).getSeconds() >= 5 ||
//                        !current.getLatitude().equals(lastAdded.getLatitude()) ||
//                        !current.getLongitude().equals(lastAdded.getLongitude())) {
//                    filteredHistories.add(current);
//                }
//            }
//        }
//
//        System.out.println("Filtered records count: " + filteredHistories.size());
//
//        return filteredHistories.stream()
//                .map(location -> new LocationHistoryDTO(
//                        location.getId(),
//                        location.getLatitude(),
//                        location.getLongitude(),
//                        location.getTimestamp()))
//                .collect(Collectors.toList());
//    }


//    @GetMapping("/getToUserLocationHistory")
//    public List<LocationHistoryDTO> getToUserLocationHistory(
//            @RequestParam String fromEmail,
//            @RequestParam String toEmail,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//
//        // Validate users and relationship
//        User fromUser = userService.findByEmail(fromEmail)
//                .orElseThrow(() -> new RuntimeException("From User not found!"));
//        User toUser = userService.findByEmail(toEmail)
//                .orElseThrow(() -> new RuntimeException("To User not found!"));
//
//        RelationUser relationUser = relationUserRepository.findByFromUserAndToUser(fromUser, toUser)
//                .orElseThrow(() -> new RuntimeException("Relationship not found!"));
//
//        if (relationUser.getIsLinked() != 1.0) {
//            throw new RuntimeException("Relationship not linked!");
//        }
//
//        // Get location history - consider asking the DB to sort it for better performance
//        List<LocationHistory> locationHistories = userService.getLocationHistoryByUserAndDate(toUser, date);
//
//        // Debug logging (consider using a proper logger instead of System.out)
//        log.debug("Total records from DB: {}", locationHistories.size());
//        if (!locationHistories.isEmpty()) {
//            log.debug("First record time: {}", locationHistories.get(0).getTimestamp());
//            log.debug("Last record time: {}", locationHistories.get(locationHistories.size()-1).getTimestamp());
//        }
//
//        // If empty, return empty list immediately
//        if (locationHistories.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        // Sort by timestamp (if not already sorted by DB)
//        locationHistories.sort(Comparator.comparing(LocationHistory::getTimestamp));
//
//        // Constants for filtering
//        final int MIN_SECONDS_BETWEEN_POINTS = 5;
//        final double MIN_DISTANCE_CHANGE_METERS = 10.0; // minimum significant distance change
//
//        List<LocationHistory> filteredHistories = new ArrayList<>();
//        filteredHistories.add(locationHistories.get(0)); // Always include first point
//
//        for (int i = 1; i < locationHistories.size(); i++) {
//            LocationHistory current = locationHistories.get(i);
//            LocationHistory lastAdded = filteredHistories.get(filteredHistories.size()-1);
//
//            long secondsBetween = Duration.between(lastAdded.getTimestamp(), current.getTimestamp()).getSeconds();
//            double distanceBetween = calculateDistance(
//                    lastAdded.getLatitude(), lastAdded.getLongitude(),
//                    current.getLatitude(), current.getLongitude());
//
//            // Include point if:
//            // 1. Enough time has passed OR
//            // 2. Significant distance moved OR
//            // 3. This is the last point in the sequence
//            if (secondsBetween >= MIN_SECONDS_BETWEEN_POINTS ||
//                    distanceBetween >= MIN_DISTANCE_CHANGE_METERS ||
//                    i == locationHistories.size() - 1) {
//                filteredHistories.add(current);
//            }
//        }
//
//        log.debug("Filtered records count: {}", filteredHistories.size());
//
//        // Convert to DTOs
//        return filteredHistories.stream()
//                .map(location -> new LocationHistoryDTO(
//                        location.getId(),
//                        location.getLatitude(),
//                        location.getLongitude(),
//                        location.getTimestamp()))
//                .collect(Collectors.toList());
//    }
//
//    // Helper method to calculate distance between two coordinates in meters
//    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//        final int R = 6371; // Radius of the earth in km
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double distance = R * c * 1000; // convert to meters
//        return distance;
//    }



//    @GetMapping("/getToUserLocationHistory")
//    public List<LocationHistoryDTO> getToUserLocationHistory(
//            @RequestParam String fromEmail,
//            @RequestParam String toEmail,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//
//        // Validate users and relationship (same as before)
//        User fromUser = userService.findByEmail(fromEmail)
//                .orElseThrow(() -> new RuntimeException("From User not found!"));
//        User toUser = userService.findByEmail(toEmail)
//                .orElseThrow(() -> new RuntimeException("To User not found!"));
//
//        RelationUser relationUser = relationUserRepository.findByFromUserAndToUser(fromUser, toUser)
//                .orElseThrow(() -> new RuntimeException("Relationship not found!"));
//
//        if (relationUser.getIsLinked() != 1.0) {
//            throw new RuntimeException("Relationship not linked!");
//        }
//
//        // Get location history
//        List<LocationHistory> locationHistories = userService.getLocationHistoryByUserAndDate(toUser, date);
//
//        // Sort by timestamp
//        locationHistories.sort(Comparator.comparing(LocationHistory::getTimestamp));
//
//        // New approach for 10-second interval filtering
//        List<LocationHistory> filteredHistories = new ArrayList<>();
//        if (!locationHistories.isEmpty()) {
//            // Add first record
//            filteredHistories.add(locationHistories.get(0));
//
//            // Get the timestamp of the first record
//            LocalDateTime lastAddedTime = locationHistories.get(0).getTimestamp();
//
//            // For subsequent records, add if at least 10 seconds have passed
//            for (int i = 1; i < locationHistories.size(); i++) {
//                LocationHistory current = locationHistories.get(i);
//                long secondsSinceLast = Duration.between(lastAddedTime, current.getTimestamp()).getSeconds();
//
//                if (secondsSinceLast >= 10) {
//                    filteredHistories.add(current);
//                    lastAddedTime = current.getTimestamp(); // Update the last added time
//                }
//            }
//        }
//
//        return filteredHistories.stream()
//                .map(location -> new LocationHistoryDTO(
//                        location.getId(),
//                        location.getLatitude(),
//                        location.getLongitude(),
//                        location.getTimestamp()))
//                .collect(Collectors.toList());
//    }

//    @GetMapping("/getToUserLocationHistory")
//    public List<LocationHistoryDTO> getToUserLocationHistory(
//            @RequestParam String fromEmail,
//            @RequestParam String toEmail,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//
//        long startTime = System.currentTimeMillis();
//
//        // Step 1: Validate users
//        User fromUser = userService.findByEmail(fromEmail)
//                .orElseThrow(() -> new RuntimeException("From User not found!"));
//        User toUser = userService.findByEmail(toEmail)
//                .orElseThrow(() -> new RuntimeException("To User not found!"));
//
//        // Step 2: Validate relationship
//        RelationUser relationUser = relationUserRepository.findByFromUserAndToUser(fromUser, toUser)
//                .orElseThrow(() -> new RuntimeException("Relationship not found!"));
//
//        if (relationUser.getIsLinked() != 1.0) {
//            throw new RuntimeException("Relationship not linked!");
//        }
//
//        // Step 3: Fetch and sort location history
//        List<LocationHistory> locationHistories = userService.getLocationHistoryByUserAndDate(toUser, date);
//
//        List<LocationHistory> filteredHistories = new ArrayList<>();
//        if (!locationHistories.isEmpty()) {
//            filteredHistories.add(locationHistories.get(0));
//            LocalDateTime lastAddedTime = locationHistories.get(0).getTimestamp();
//
//            for (int i = 1; i < locationHistories.size(); i++) {
//                LocationHistory current = locationHistories.get(i);
//                long secondsSinceLast = Duration.between(lastAddedTime, current.getTimestamp()).getSeconds();
//
//                if (secondsSinceLast >= 10) {
//                    filteredHistories.add(current);
//                    lastAddedTime = current.getTimestamp();
//                }
//            }
//        }
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("Location history response time: " + (endTime - startTime) + " ms");
//
//        // Step 4: Return DTOs
//        return filteredHistories.stream()
//                .map(location -> new LocationHistoryDTO(
//                        location.getId(),
//                        location.getLatitude(),
//                        location.getLongitude(),
//                        location.getTimestamp()))
//                .collect(Collectors.toList());
//    }



//    @GetMapping("/getToUserLocationHistory")
//    public List<LinkedUserLocationHistoryDTO> getLinkedUsersLocationHistory(
//            @RequestParam String fromEmail,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//
//        long startTime = System.currentTimeMillis();
//
//        // Step 1: Validate main user
//        User fromUser = userService.findByEmail(fromEmail)
//                .orElseThrow(() -> new RuntimeException("User not found!"));
//
//        // Step 2: Get all linked users (where isLinked = 1.0)
//        List<RelationUser> linkedRelations = relationUserRepository.findByFromUserAndIsLinked(fromUser, 1.0);
////        RelationUser linkedRelations = relationUserRepository.findByFromUserAndToUser(fromUser, toUser)
////                .orElseThrow(() -> new RuntimeException("Relationship not found!"));
//
//        if (linkedRelations.isEmpty()) {
//            throw new RuntimeException("No linked users found!");
//        }
//
//        // Step 3: Process each linked user's location history
//        List<LinkedUserLocationHistoryDTO> result = new ArrayList<>();
//
//        for (RelationUser relation : linkedRelations) {
//            User linkedUser = relation.getToUser();
//
//            // Get all locations for the date
//            List<LocationHistory> allLocations = userService.getLocationHistoryByUserAndDate(linkedUser, date);
//
//            if (!allLocations.isEmpty()) {
//                // Sort by timestamp if not already sorted
//                allLocations.sort(Comparator.comparing(LocationHistory::getTimestamp));
//
//                // Filter locations - start with first point, then add points when moved ~3 meters
//                List<LocationHistory> filteredLocations = new ArrayList<>();
//                filteredLocations.add(allLocations.get(0));
//
//                // Using first location as reference
//                LocationHistory lastAdded = allLocations.get(0);
//
//                for (int i = 1; i < allLocations.size(); i++) {
//                    LocationHistory current = allLocations.get(i);
//
//                    // Calculate distance from last added point (in meters)
//                    double distance = calculateDistance(
//                            lastAdded.getLatitude(), lastAdded.getLongitude(),
//                            current.getLatitude(), current.getLongitude()
//                    );
//
//                    // If moved more than ~3 meters, add to filtered list
//                    if (distance >= 3.0) {
//                        filteredLocations.add(current);
//                        lastAdded = current;
//                    }
//                }
//
//                // Create DTO for this linked user
//                LinkedUserLocationHistoryDTO userHistory = new LinkedUserLocationHistoryDTO();
////                userHistory.setUserEmail(linkedUser.getEmail());
////                userHistory.setUserName(linkedUser.relations()); // assuming User has getName()
//
//                userHistory.setLocations(filteredLocations.stream()
//                        .map(loc -> new LocationHistoryDTO(
//                                loc.getId(),
//                                loc.getLatitude(),
//                                loc.getLongitude(),
//                                loc.getTimestamp()))
//                        .collect(Collectors.toList()));
//
//                result.add(userHistory);
//            }
//        }
//
//        long endTime = System.currentTimeMillis();
//        System.out.println("API response time: " + (endTime - startTime) + " ms");
//
//        return result;
//    }


@GetMapping("/getToUserLocationHistory")
public List<LocationHistoryDTO> getLinkedUsersLocationHistory(
        @RequestParam String fromEmail,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    long startTime = System.currentTimeMillis();

    // Step 1: Validate main user
    User fromUser = userService.findByEmail(fromEmail)
            .orElseThrow(() -> new RuntimeException("User not found!"));

    // Step 2: Get all linked users (where isLinked = 1.0)
    List<RelationUser> linkedRelations = relationUserRepository.findByFromUserAndIsLinked(fromUser, 1.0);

    if (linkedRelations.isEmpty()) {
        throw new RuntimeException("No linked users found!");
    }

    // Step 3: Collect all LocationHistoryDTOs
    List<LocationHistoryDTO> result = new ArrayList<>();

    for (RelationUser relation : linkedRelations) {
        User linkedUser = relation.getToUser();

        // Get all locations for the date
        List<LocationHistory> allLocations = userService.getLocationHistoryByUserAndDate(linkedUser, date);

        if (!allLocations.isEmpty()) {
            // Sort by timestamp
            allLocations.sort(Comparator.comparing(LocationHistory::getTimestamp));

            List<LocationHistory> filteredLocations = new ArrayList<>();
            filteredLocations.add(allLocations.get(0));

            LocationHistory lastAdded = allLocations.get(0);

            for (int i = 1; i < allLocations.size(); i++) {
                LocationHistory current = allLocations.get(i);

                double distance = calculateDistance(
                        lastAdded.getLatitude(), lastAdded.getLongitude(),
                        current.getLatitude(), current.getLongitude()
                );

                if (distance >= 3.0) {
                    filteredLocations.add(current);
                    lastAdded = current;
                }
            }

            // Add all filtered locations to the result
            result.addAll(
                    filteredLocations.stream()
                            .map(loc -> new LocationHistoryDTO(
                                    loc.getId(),
                                    loc.getLatitude(),
                                    loc.getLongitude(),
                                    loc.getTimestamp()))
                            .collect(Collectors.toList())
            );
        }
    }

    long endTime = System.currentTimeMillis();
    System.out.println("API response time: " + (endTime - startTime) + " ms");

    return result;
}


    // Haversine formula to calculate distance between two points in meters
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }

    // Add this to your RelationUserRepository

}