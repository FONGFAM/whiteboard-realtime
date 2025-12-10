package com.whiteboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

     @Bean
     public CorsConfigurationSource corsConfigurationSource() {
          CorsConfiguration configuration = new CorsConfiguration();
          configuration.setAllowCredentials(false); // Important: don't allow credentials with "*"
          configuration.addAllowedOriginPattern("*"); // Allow all origins
          configuration.addAllowedMethod("*"); // Allow all HTTP methods
          configuration.addAllowedHeader("*"); // Allow all headers

          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", configuration);
          return source;
     }
}
