package com.maarco.websocket;

import java.util.List;

/**
 * Request DTO for checking WebSocket connection status.
 * Supports checking connection status for either:
 * - A single user (via userId)
 * - Multiple users (via userIds list)
 *
 * <p>Used in WebSocket operations to verify active connections
 * before sending messages or notifications.</p>
 */
public class ConnectionCheckRequest {
    private String userId;  // For single user check
    private List<String> userIds;  // For multiple users check

    // Constructors
    public ConnectionCheckRequest() {
    }

    public ConnectionCheckRequest(String userId) {
        this.userId = userId;
    }

    public ConnectionCheckRequest(List<String> userIds) {
        this.userIds = userIds;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}