package com.maarco.websocket;//package com.maarco.websocket;

import com.maarco.entities.User;
import com.maarco.repository.RelationUserRepository;
import com.maarco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketConnectionTracker {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Autowired
    private RelationUserRepository relationUserRepository;

    // Track active sessions and their associated users
    private final Map<String, String> sessionToUserMap = new ConcurrentHashMap<>();
    private final Map<String, String> userToSessionMap = new ConcurrentHashMap<>();

    public void handleConnect(String sessionId, String userId) {
        // Update mappings
        sessionToUserMap.put(sessionId, userId);
        userToSessionMap.put(userId, sessionId);

        // Update user in database
        userRepository.findById(userId).ifPresent(user -> {
            user.setConnected(true);
            user.setCurrentSessionId(sessionId);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);

            System.out.println("User connected: " + userId + " (session: " + sessionId + ")");
        });
    }

    public void handleDisconnect(String sessionId) {
        String userId = sessionToUserMap.get(sessionId);
        if (userId != null) {
            // Update user in database
            userRepository.findById(userId).ifPresent(user -> {
                // Only update if this is the most recent session
                if (sessionId.equals(user.getCurrentSessionId())) {
                    user.setConnected(false);
                    user.setLastSeen(LocalDateTime.now());
                    userRepository.save(user);

                    System.out.println("User disconnected: " + userId + " (session: " + sessionId + ")");
                }
            });

            // Clean up mappings
            sessionToUserMap.remove(sessionId);
            userToSessionMap.remove(userId);
        }
    }


    public void cleanupExpiredSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);

        // Find users who haven't been seen in a while but are still marked as connected
        List<User> expiredUsers = userRepository.findByConnectedTrueAndLastSeenBefore(threshold);

        for (User user : expiredUsers) {
            System.out.println("Cleaning up expired session for user: " + user.getUserId());
            user.setConnected(false);
            userRepository.save(user);

            // Clean up mappings if they exist
            String sessionId = userToSessionMap.get(user.getUserId());
            if (sessionId != null) {
                sessionToUserMap.remove(sessionId);
                userToSessionMap.remove(user.getUserId());
            }
        }
    }



}