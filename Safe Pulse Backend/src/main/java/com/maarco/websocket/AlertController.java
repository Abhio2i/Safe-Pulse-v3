//package com.maarco.websocket;
//
//
//import com.maarco.entities.User;
//import com.maarco.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//public class AlertController {
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @MessageMapping("/send-alert")
//    public void handleEmergencyAlert(AlertMessage alert) {
//        // Get the user who sent the alert
//        User sender = userRepository.findByUserId(alert.getUserId());
//        if (sender == null) return;
//
//        // Get all users connected to this user
//        List<User> connectedUsers = userRepository.findConnectedUsers(alert.getUserId());
//
//        // Send alert to each connected user
//        for (User recipient : connectedUsers) {
//            Map<String, Object> alertDetails = new HashMap<>();
//            alertDetails.put("type", "EMERGENCY");
//            alertDetails.put("message", alert.getMessage());
//            alertDetails.put("senderId", sender.getUserId());
//            alertDetails.put("senderEmail", sender.getEmail());
//            alertDetails.put("senderName", sender.getUserName());
//            alertDetails.put("timestamp", LocalDateTime.now().toString());
//            alertDetails.put("location", alert.getLocation());
//
//            messagingTemplate.convertAndSendToUser(
//                    recipient.getUserId(),
//                    "/queue/emergency",
//                    alertDetails
//            );
//        }
//    }
//
//    public static class AlertMessage {
//        private String userId;
//        private String message;
//        private Map<String, Double> location;
//
//        // Getters and setters
//        public String getUserId() { return userId; }
//        public void setUserId(String userId) { this.userId = userId; }
//        public String getMessage() { return message; }
//        public void setMessage(String message) { this.message = message; }
//        public Map<String, Double> getLocation() { return location; }
//        public void setLocation(Map<String, Double> location) { this.location = location; }
//    }
//}