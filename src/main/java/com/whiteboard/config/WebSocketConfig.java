package com.whiteboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

     @Override
     public void configureMessageBroker(MessageBrokerRegistry config) {
          // Enable simple memory-based message broker for topics
          config.enableSimpleBroker("/topic", "/queue");

          // Prefix for messages routed to @MessageMapping methods
          config.setApplicationDestinationPrefixes("/app");

          // Optional: set user destination prefix
          config.setUserDestinationPrefix("/user");
     }

     @Override
     public void registerStompEndpoints(StompEndpointRegistry registry) {
          // Register STOMP endpoint with SockJS fallback
          registry.addEndpoint("/ws-whiteboard")
                    .setAllowedOrigins("*")
                    .withSockJS();
     }
}
