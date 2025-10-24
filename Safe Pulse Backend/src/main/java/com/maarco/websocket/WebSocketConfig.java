package com.maarco.websocket;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.maarco.entities.RelationUser;
import com.maarco.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();
    private final Map<String, Boolean> userConnectionStatus = new ConcurrentHashMap<>();

    @Autowired
    private WebSocketConnectionTracker connectionTracker;



    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
//        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }



    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Allow CORS for the WebSocket endpoint
        registry.addEndpoint("/ws-location")
//                .setAllowedOrigins("http://127.0.0.1:5500") // Allow requests from this origin
//                .setAllowedOrigins("*")
                .setAllowedOriginPatterns("*") // ✅ Allows all origins properly

                .withSockJS();
    }



    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                String userId = accessor.getFirstNativeHeader("userId");
                String sessionId = accessor.getSessionId();

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    if (userId != null && sessionId != null) {
                        // Handle new connection
                        connectionTracker.handleConnect(sessionId, userId);
                    }
                }
                else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    if (sessionId != null) {
                        // Handle disconnection
                        connectionTracker.handleDisconnect(sessionId);
                    }
                }
                return message;
            }
        });
    }

    public boolean isUserConnected(String userId) {
        return Boolean.TRUE.equals(userConnectionStatus.get(userId));
    }


}