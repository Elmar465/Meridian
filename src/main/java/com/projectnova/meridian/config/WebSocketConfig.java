package com.projectnova.meridian.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer  {


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        //Enable a simple in-memory message broker
        //Clients subscribe to /topic for broadcast
        registry.enableSimpleBroker("/topic", "/queue");

        //Messages sent to /app will be routed routed @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry  registry) {
        //WebSocket endpoint that clients connect to
        registry
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS(); // Fallback for browsers that don't support WebSocket
    }
}
