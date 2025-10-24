
/**
 * WebSocket controller for managing and checking user connection statuses.
 * <p>
 * Features:
 * - Provides real-time user connectivity status updates
 * - Supports checking status of all users or specific user groups
 * - Integrates with database for persistent status tracking
 * - Uses STOMP messaging protocol for WebSocket communication
 * <p>
 * Endpoints:
 * - /app/check-all-connection-status (broadcast to /topic/user-connection-updates)
 * - /app/check-multiple-connection-status (broadcast to /topic/connection-status-updates)
 * <p>
 * Security:
 * - All methods should be protected with authentication
 * - Consider adding @PreAuthorize annotations for role-based access
 */
package com.maarco.websocket;

import com.maarco.entities.User;
import com.maarco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ConnectionStatusController {

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Autowired
    private UserRepository userRepository;

    // For checking single user status
//    @MessageMapping("/check-all-connection-status")
//    @SendTo("/topic/user-connection-updates")
//    public List<ConnectionStatus> checkAllConnectionStatus() {
//        // Get all users from repository
//        List<User> allUsers = userRepository.findAll();
//
//        System.out.printf("++++++++++++++---------status-------------------+++++++++++++++++++"+allUsers);
//
//        return allUsers.stream()
//                .map(user -> new ConnectionStatus(
//                        user.getUserId(),
//                        webSocketConfig.isUserConnected(user.getUserId()),
//                        user.getLastSeen()
//                ))
//                .collect(Collectors.toList());
//    }
//
//    // For checking multiple users at once
//    @MessageMapping("/check-multiple-connection-status")
//    @SendTo("/topic/connection-status-updates")
//    public List<ConnectionStatus> checkMultipleConnectionStatus(ConnectionCheckRequest request) {
//        return request.getUserIds().stream()
//                .map(userId -> new ConnectionStatus(userId, webSocketConfig.isUserConnected(userId)))
//                .collect(Collectors.toList());
//    }


    @MessageMapping("/check-all-connection-status")
    @SendTo("/topic/user-connection-updates")
    public List<ConnectionStatus> checkAllConnectionStatus() {
        // Get all users from repository with their actual connection status
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .map(user -> new ConnectionStatus(
                        user.getUserId(),
                        user.isConnected(), // Get from DB instead of session map
                        user.getLastSeen() != null ? user.getLastSeen().toString() : null
                ))
                .collect(Collectors.toList());
    }

    @MessageMapping("/check-multiple-connection-status")
    @SendTo("/topic/connection-status-updates")
    public List<ConnectionStatus> checkMultipleConnectionStatus(ConnectionCheckRequest request) {
        return request.getUserIds().stream()
                .map(userId -> {
                    User user = userRepository.findById(userId).orElse(null);
                    System.out.printf(":::::::::::::::dsadsdsdsds::::::::::::::::---");

                    if (user != null) {
                        return new ConnectionStatus(
                                userId,
                                user.isConnected(),
                                user.getLastSeen() != null ? user.getLastSeen().toString() : null
                        );
                    }
                    return new ConnectionStatus(userId, false, null);
                })
                .collect(Collectors.toList());
    }
}