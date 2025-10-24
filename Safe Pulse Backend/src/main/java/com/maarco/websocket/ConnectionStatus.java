
/**
 * Data transfer object (DTO) representing a user's connection status.
 * Used to communicate real-time connectivity information between server and clients via WebSocket.
 *
 * Contains:
 * - User identification
 * - Current connection state (online/offline)
 * - Last activity timestamp
 *
 * Usage:
 * - Broadcast via /topic/user-connection-updates
 * - Sent in response to status check requests
 */

package com.maarco.websocket;

public class ConnectionStatus {
    private String userId;
    private boolean connected;
    private String lastSeen; // Make sure this field exists

    // All-args constructor
    public ConnectionStatus(String userId, boolean connected, String lastSeen) {
        this.userId = userId;
        this.connected = connected;
        this.lastSeen = lastSeen;
    }

    // Getters and setters for ALL fields
    public String getUserId() {
        return userId;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getLastSeen() {
        return lastSeen;
    }



}