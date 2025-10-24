package com.maarco.websocket;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RelationStatus {
    private String fromUserId;
    private String toUserId;
    private boolean fromUserConnected;
    private boolean toUserConnected;
    private LocalDateTime toUserLastSeen;

    public RelationStatus(String fromUserId, String toUserId,
                          boolean fromUserConnected, boolean toUserConnected,
                          LocalDateTime toUserLastSeen) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.fromUserConnected = fromUserConnected;
        this.toUserConnected = toUserConnected;
        this.toUserLastSeen = toUserLastSeen;
    }
}