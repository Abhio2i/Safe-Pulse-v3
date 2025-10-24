


//
//
//package com.maarco.geofrence;
//
//import com.maarco.entities.LocationHistory;
//import com.maarco.entities.User;
//import com.maarco.repository.UserRepository;
//import com.maarco.websocket.WebSocketController;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//public class ZoneAlertService {
//
//    @Autowired
//    private WebSocketController webSocketController;
//    private final SafeZoneRepository safeZoneRepository;
//    private final UserRepository userRepository;
//    private final SimpMessagingTemplate messagingTemplate;
//
//    // Track user-zone state to avoid duplicate alerts
//    private final Map<String, Map<String, Boolean>> userZoneStates = new ConcurrentHashMap<>();
//
//    public ZoneAlertService(SafeZoneRepository safeZoneRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
//        this.safeZoneRepository = safeZoneRepository;
//        this.userRepository = userRepository;
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    public void checkZoneCrossing(LocationHistory location) {
//        User user = location.getUser();
//        if (user == null) {
//            return;
//        }
//
//        String userEmail = user.getEmail();
//        String userId = user.getUserId();
//
//        // Find zones where the user is either the creator or in sharedWith
//        List<SafeZone> relevantZones = safeZoneRepository.findByCreatedByOrSharedWithContaining(userEmail, userEmail);
//
//        for (SafeZone zone : relevantZones) {
//            // Check if the user is in the sharedWith list or is the creator
//            if (!zone.getSharedWith().contains(userEmail) && !zone.getCreatedBy().equals(userEmail)) {
//                continue;
//            }
//
//            double distance = calculateDistance(
//                    location.getLatitude(), location.getLongitude(),
//                    zone.getLatitude(), zone.getLongitude());
//
//            boolean isInside = distance <= zone.getRadius();
//
//            // Get or initialize user-zone state
//            userZoneStates.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
//            Boolean previousState = userZoneStates.get(userId).getOrDefault(zone.getId(), null);
//
//            // Skip if state hasn't changed
//            if (previousState != null && previousState == isInside) {
//                continue;
//            }
//
//            // Update state
//            userZoneStates.get(userId).put(zone.getId(), isInside);
//
//            // Trigger alerts based on zone type and state change
//            if (zone.getType() == SafeZone.ZoneType.SAFE && !isInside && (previousState == null || previousState)) {
//                // User left safe zone
//                sendAlert(zone, user, location, "left safe zone");
//            } else if (zone.getType() == SafeZone.ZoneType.DANGER && isInside && (previousState == null || !previousState)) {
//                // User entered danger zone
//                sendAlert(zone, user, location, "entered danger zone");
//            }
//        }
//    }
//
//    private void sendAlert(SafeZone zone, User user, LocationHistory location, String action) {
//
//        System.out.printf("Zone Alert---------------------------");
//        String alertMessage = String.format(
//                "User %s has %s '%s' (%.2fm from center)",
//                user.getEmail(), action, zone.getName(),
//                calculateDistance(
//                        location.getLatitude(), location.getLongitude(),
//                        zone.getLatitude(), zone.getLongitude()
//                ));
//
//        // Send alert to the user's WebSocket topic
//        messagingTemplate.convertAndSend(
//                "/topic/alerts-" + user.getUserId(),
//                Map.of(
//                        "userId", user.getUserId(),
//                        "message", alertMessage,
//                        "timestamp", LocalDateTime.now()
//                )
//        );
//
//        // Send to the creator if different from the user
//        if (!zone.getCreatedBy().equals(user.getEmail())) {
//            userRepository.findByEmail(zone.getCreatedBy()).ifPresent(creator -> {
//                messagingTemplate.convertAndSend(
//                        "/topic/alerts-" + creator.getUserId(),
//                        Map.of(
//                                "userId", creator.getUserId(),
//                                "message", alertMessage,
//                                "timestamp", LocalDateTime.now()
//                        )
//                );
//            });
//        }
//
//        // Implement other alert methods based on SOS settings
//        if (zone.getSosSettings().isNotification()) {
//            // TODO: Send push notification using user.getNotificationToken()
//        }
//        if (zone.getSosSettings().isMessage()) {
//            // TODO: Send SMS using user.getMobileNo()
//        }
//    }
//
//    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//        final int R = 6371; // Earth radius in km
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return R * c * 1000; // Convert to meters
//    }
//}




package com.maarco.geofrence;

import com.maarco.entities.LocationHistory;
import com.maarco.entities.User;
import com.maarco.repository.UserRepository;
import com.maarco.websocket.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ZoneAlertService {

    @Autowired
    private WebSocketController webSocketController;
    private final SafeZoneRepository safeZoneRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Track user-zone state to avoid duplicate alerts
    private final Map<String, Map<String, Boolean>> userZoneStates = new ConcurrentHashMap<>();

    public ZoneAlertService(SafeZoneRepository safeZoneRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.safeZoneRepository = safeZoneRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void checkZoneCrossing(LocationHistory location) {
        User user = location.getUser();
        if (user == null) {
            return;
        }

        String userEmail = user.getEmail();
        String userId = user.getUserId();

        // Find zones where the user is in sharedWith
        List<SafeZone> relevantZones = safeZoneRepository.findBySharedWithContaining(userEmail);

        for (SafeZone zone : relevantZones) {
            // Only process if the user is in sharedWith
            if (!zone.getSharedWith().contains(userEmail)) {
                continue;
            }

            double distance = calculateDistance(
                    location.getLatitude(), location.getLongitude(),
                    zone.getLatitude(), zone.getLongitude());

            boolean isInside = distance <= zone.getRadius();

            // Get or initialize user-zone state
            userZoneStates.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
            Boolean previousState = userZoneStates.get(userId).getOrDefault(zone.getId(), null);

            // Skip if state hasn't changed
            if (previousState != null && previousState == isInside) {
                continue;
            }

            // Update state
            userZoneStates.get(userId).put(zone.getId(), isInside);

            // Trigger alerts based on zone type and state change
            if (zone.getType() == SafeZone.ZoneType.SAFE && !isInside && (previousState == null || previousState)) {
                // User left safe zone
                sendAlert(zone, user, location, "left safe zone");
            } else if (zone.getType() == SafeZone.ZoneType.DANGER && isInside && (previousState == null || !previousState)) {
                // User entered danger zone
                sendAlert(zone, user, location, "entered danger zone");
            }
        }
    }

    private void sendAlert(SafeZone zone, User user, LocationHistory location, String action) {
        System.out.println("Zone Alert---------------------------");
        System.out.println("User---------------------------"+user.getEmail());
        System.out.println("User---------------------------"+user.getUserId());

        // Safe way to get user's display name
        String displayName = user.getUserName(); // Default to username
        if (user.getUserProfile() != null) {
            displayName = user.getUserProfile().getFirstName() != null
                    ? user.getUserProfile().getFirstName()
                    : displayName;
        }

        double distance = calculateDistance(
                location.getLatitude(), location.getLongitude(),
                zone.getLatitude(), zone.getLongitude()
        );



//        String alertMessage = String.format(
//                "User %s has %s '%s' (%.2fm from center)",
////                user.getEmail(),
//                user.getUserProfile().getFirstName(),
////                user.getUserProfile().getProfileImg() != null && !user.getUserProfile().getProfileImg().isBlank()
////                        ? user.getUserProfile().getProfileImg()
////                        : "http://localhost:7072/images/default-user-img.jpg", // fallback to default image
//                action,
//                zone.getName(),
//                calculateDistance(
//                        location.getLatitude(), location.getLongitude(),
//                        zone.getLatitude(), zone.getLongitude()
//                ));

        String alertMessage = String.format(
                "User %s has %s '%s' (%.2fm from center)",
                displayName,
                action,
                zone.getName(),
                distance
        );

        System.out.println("Alert Message: " + alertMessage);


        System.out.println("mesages-------------alert-------------"+alertMessage); // 👈 Final toString-like print of all details

        // Send alert only to the user's WebSocket topic
        messagingTemplate.convertAndSend(
                "/topic/alerts-" + user.getUserId(),
                Map.of(
                        "userId", user.getUserId(),
                        "message", alertMessage,
                        "timestamp", LocalDateTime.now()
                )
        );

        // Implement other alert methods based on SOS settings
        if (zone.getSosSettings().isNotification()) {
            // TODO: Send push notification using user.getNotificationToken()
        }
        if (zone.getSosSettings().isMessage()) {
            // TODO: Send SMS using user.getMobileNo()
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Convert to meters
    }
}