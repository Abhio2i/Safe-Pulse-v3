package com.maarco.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSessionCleanup {

    @Autowired
    private WebSocketConnectionTracker connectionTracker;

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredSessions() {
        connectionTracker.cleanupExpiredSessions();
    }
}
