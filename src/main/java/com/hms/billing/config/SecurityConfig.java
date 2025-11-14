package com.hms.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())        // disable CSRF for Postman testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()        // allow ALL endpoints without auth
                )
                .oauth2ResourceServer(oauth -> oauth.disable()); // disable JWT auth entirely

        return http.build();
    }
}