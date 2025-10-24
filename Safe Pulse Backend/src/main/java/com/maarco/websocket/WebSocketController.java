
package com.maarco.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maarco.entities.LocationHistory;
import com.maarco.entities.RelationUser;
import com.maarco.entities.User;
import com.maarco.exception.UserNotFoundException;
import com.maarco.geofrence.ZoneAlertService;
import com.maarco.repository.RelationUserRepository;
import com.maarco.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class WebSocketController {

    @Autowired
    private LocationHistoryService locationHistoryService;

    @Autowired
    private UserRepository userRepository; // Assuming you have a UserRepository

    @PostMapping("/bulk-location-save")
    public ResponseEntity<String> saveBulkLocations(
            @RequestParam String username,
            @RequestBody List<LocationHistory> locationHistories) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + username));

        for (LocationHistory locationHistory : locationHistories) {
            locationHistory.setUser(user);
        }

        locationHistoryService.saveAllLocations(locationHistories);

        return ResponseEntity.ok("Data is successfully saved");
    }


    @MessageMapping("/request-status")
    @SendTo("/topic/connection-updates")
    public List<ConnectionStatus> sendInitialStatus() {
        List<User> allUsers = userRepository.findAll();
        System.out.printf(":::::::::::::::dsadsdsdsds rr::::::::::::::::---");


        return allUsers.stream()
                .map(user -> new ConnectionStatus(
                        user.getUserId(),
                        user.isConnected(),
                        user.getLastSeen() != null ? user.getLastSeen().toString() : null
                ))
                .collect(Collectors.toList());
    }


// last update 15 may

//    @MessageMapping("/update-location") // Endpoint for receiving location updates
//    @SendTo("/topic/location-updates") // Broadcast to all subscribers of this topic
//    public LocationHistory updateLocation(LocationUpdateRequest request) {
//        // Fetch the user from the database
//        User user = userRepository.findByUserId(request.getUserId());
//
//        if (user == null) {
//            throw new UserNotFoundException("Data not Detected-----");
//        }
//
//        System.out.println("User Web Sock ---------"+user);
//        // Create a new LocationHistory object
//        LocationHistory locationHistory = new LocationHistory();
//        locationHistory.setLatitude(request.getLatitude());
//        locationHistory.setLongitude(request.getLongitude());
//        locationHistory.setUser(user);
//
//        // Save the location history to MongoDB
//        return locationHistoryService.saveLocation(locationHistory);
//    }


    // For noSaigal to subscribe to abhishek's locations
//     @MessageMapping("/subscribe-to-user")
//     @SendTo("/topic/user-locations")
//     public List<LocationHistory> subscribeToUser(String userIdToTrack) {
//         // Return last 50 locations of the tracked user
//         List<LocationHistory> last50LocationsByUserId = locationHistoryService.findLast50LocationsByUserId(userIdToTrack);

//         System.out.println("Tracked locations by " + userIdToTrack + ":");
//         for (LocationHistory location : last50LocationsByUserId) {
//             System.out.println(
//                     "Lat: " + location.getLatitude() +
//                             ", Long: " + location.getLongitude() +
//                             ", Time: " + location.getTimestamp()
//             );
//         }

    // //        System.out.println("Tracked locations by " + userIdToTrack + ":");
// //        for (LocationHistory location : last50LocationsByUserId) {
// //            System.out.printf(
// //                    "Lat: %f, Long: %f, Time: %s%n",
// //                    location.latitude(),
// //                    location.longitude(),
// //                    location.timestamp()
// //            );
// //        }
//         return last50LocationsByUserId;
//     }
    @Autowired
    private ZoneAlertService zoneAlertService;
    @Autowired
    @Lazy
    private SimpMessagingTemplate messagingTemplate;



//
//    @MessageMapping("/update-location")
//    @SendTo("/topic/location-updates")
//    public LocationHistory updateLocation(LocationUpdateRequest request) {
//        User user = userRepository.findByUserId(request.getUserId());
//        if (user == null) {
//            throw new UserNotFoundException("User not found--------------------");
//        }
//
//
//        System.out.printf("---gfgfd-------------");
//        // Create and save location history
//        LocationHistory locationHistory = new LocationHistory();
//        locationHistory.setLatitude(request.getLatitude());
//        locationHistory.setLongitude(request.getLongitude());
//        locationHistory.setUser(user);
////        locationHistory.setTimestamp(LocalDateTime.now());
//        locationHistory.setTimestamp(request.getTimestamp());

    /// /        System.out.printf("hdgsf ttt--------",locationHistory.setTimestamp(request.getTimestamp()));
//
//        // Save to database and update status
//        return locationHistoryService.saveLocation(locationHistory);
//    }
    @MessageMapping("/update-location")
    @SendTo("/topic/location-updates")
    public LocationHistory updateLocation(LocationUpdateRequest request) {
        User user = userRepository.findByUserId(request.getUserId());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        // Do not update user's location fields to avoid null issues
        // user.setLatitude(request.getLatitude());
        // user.setLongitude(request.getLongitude());
        // userRepository.save(user);

        // Create and save location history
        LocationHistory locationHistory = new LocationHistory();
        locationHistory.setLatitude(request.getLatitude());
        locationHistory.setLongitude(request.getLongitude());
        locationHistory.setUser(user);
        locationHistory.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());

        LocationHistory savedHistory = locationHistoryService.saveLocation(locationHistory);

        // Check for zone crossings
        zoneAlertService.checkZoneCrossing(savedHistory);

        return savedHistory;
    }

//    public void sendAlertToTracker(String userId, String message) {
//        messagingTemplate.convertAndSend("/topic/alerts-" + userId,
//                Map.of("userId", userId, "message", message, "timestamp", LocalDateTime.now()));
//    }
    public void sendAlertToTracker(String userId, String message) {
        messagingTemplate.convertAndSend(
                "/topic/alerts-" + userId,
                Map.of(
                        "userId", userId,
                        "message", message,
                        "timestamp", LocalDateTime.now()
                )
        );
    }

    @Autowired
    private RelationUserRepository relationUserRepository; // You'll need this to check relationships


    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);


    @MessageMapping("/subscribe-to-multiple-users")
    @SendTo("/topic/multiple-user-locations")
    public Object subscribeToMultipleUsers(MultiUserSubscribeRequest request) {
        System.out.println("hgjhgjgjh --------------- ");
        try {
            // Verify requesting user exists
            User requestingUser = userRepository.findByUserId(request.getRequestingUserId());
            if (requestingUser == null) {
                return Map.of("error", "Requesting user not found");
            }

            Map<String, List<LocationHistory>> result = new HashMap<>();

            for (String userIdToTrack : request.getUserIdsToTrack()) {
                User userToTrack = userRepository.findByUserId(userIdToTrack);
                if (userToTrack == null) {
                    result.put(userIdToTrack, Collections.emptyList());
                    continue;
                }

                // Check relationship in both directions
                Optional<RelationUser> relation = relationUserRepository.findByFromUserAndToUser(
                        requestingUser,
                        userToTrack
                );

                Optional<RelationUser> reverseRelation = relationUserRepository.findByFromUserAndToUser(
                        userToTrack,
                        requestingUser
                );

                // Check if either relationship exists and is linked
                boolean isLinked = (relation.isPresent() && relation.get().getIsLinked() == 1.0) ||
                        (reverseRelation.isPresent() && reverseRelation.get().getIsLinked() == 1.0);

                if (!isLinked) {
                    result.put(userIdToTrack, Collections.emptyList());
                    continue;
                }


                System.out.println("fsdjkfbhsdkf--------------fmdsnb f,mdsf");

                // Get locations from last 2 hours
                List<LocationHistory> locations = locationHistoryService.findLocationsFromLast2HoursByUserId(userIdToTrack);
                log.debug("Locations for user {}: {}", userIdToTrack, locations);  // Using logger

                result.put(userIdToTrack, locations);
            }

            return result;

        } catch (Exception e) {
            log.error("Error processing multi-user tracking request", e);
            return Map.of("error", "Error processing tracking request");
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MultiUserSubscribeRequest {
        private String requestingUserId;
        private List<String> userIdsToTrack;
    }

    @MessageMapping("/subscribe-to-user")
    @SendTo("/topic/user-locations")
    public Object subscribeToUser(SubscribeRequest request) {
        try {
            // Verify both users exist
            User requestingUser = userRepository.findByUserId(request.getRequestingUserId());
            User userToTrack = userRepository.findByUserId(request.getUserIdToTrack());

            if (requestingUser == null || userToTrack == null) {
                return Map.of("error", "User not found");
            }

            // Check if there's a linked relationship in either direction
            Optional<RelationUser> relation = relationUserRepository.findByFromUserAndToUser(
                    requestingUser,
                    userToTrack
            );

            // Also check the reverse relationship
            Optional<RelationUser> reverseRelation = relationUserRepository.findByFromUserAndToUser(
                    userToTrack,
                    requestingUser
            );

            // Check if either relationship exists and is linked
            boolean isLinked = (relation.isPresent() && relation.get().getIsLinked() == 1.0) ||
                    (reverseRelation.isPresent() && reverseRelation.get().getIsLinked() == 1.0);

            if (!isLinked) {
                return Map.of("error", "User is not linked or relationship doesn't exist");
            }

            // If linked, get the location history
//            List<LocationHistory> locations = locationHistoryService.findLast50LocationsByUserId(request.getUserIdToTrack());

            // Get locations from last 2 hours instead of last 50
            List<LocationHistory> locations = locationHistoryService.findLocationsFromLast2HoursByUserId(request.getUserIdToTrack());

            // Print the locations to console/log
            System.out.println("Last 50 locations for user " + request.getUserIdToTrack() + ":");
            locations.forEach(location -> System.out.println(location.toString()));

            // Or using logger
            log.info("Last 50 locations for user {}: {}", request.getUserIdToTrack(), locations);

            return locations;

        } catch (Exception e) {
            log.error("Error processing tracking request", e);
            return Map.of("error", "Error processing tracking request");
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubscribeRequest {
        private String requestingUserId;
        private String userIdToTrack;
    }


}


// A new class to represent the request payload
class LocationUpdateRequest {
    private Double latitude;
    private Double longitude;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;
    private String userId;

    // Getters and setters
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @MessageMapping("/signal")
    @SendTo("/topic/signal")
    public SignalMessage handleSignal(SignalMessage message) {
        // Relay the signaling message to the other user
        return message;
    }
}


class SignalMessage {
    private String fromUserId;
    private String toUserId;
    private String type; // "offer", "answer", "candidate"
    private Object data; // SDP or ICE candidate

    // Getters and setters
    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}