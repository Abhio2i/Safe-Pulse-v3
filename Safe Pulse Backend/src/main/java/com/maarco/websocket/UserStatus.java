package com.maarco.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
//@AllArgsConstructor
public class UserStatus {
    private String userId;
    private boolean online;
    private String lastSeen;  // Use ISO-8601 format
//    private String status;    // "active", "idle", or "offline"

    // Constructors, getters, setters
    public UserStatus(String userId, boolean online, String lastSeen) {
        this.userId = userId;
        this.online = online;
        this.lastSeen = lastSeen;
    }

////     Full constructor
//    public UserStatus(String userId, boolean online, String lastSeen, String status) {
//        this.userId = userId;
//        this.online = online;
//        this.lastSeen = lastSeen;
//        this.status = status;
//    }
//
//    // Backward-compatible constructor
//    public UserStatus(String userId, boolean online, String lastSeen) {
//        this(userId, online, lastSeen, online ? "active" : "offline");
//    }

}