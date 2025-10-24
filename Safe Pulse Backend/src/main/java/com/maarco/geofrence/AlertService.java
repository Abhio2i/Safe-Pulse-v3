//package com.maarco.geofrence;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@Service
//public class AlertService {
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    public void sendAlert(String userId, String message) {
//        messagingTemplate.convertAndSend("/topic/alerts-" + userId,
//                Map.of("userId", userId, "message", message, "timestamp", LocalDateTime.now()));
//    }
//}