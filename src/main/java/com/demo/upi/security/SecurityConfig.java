package com.demo.upi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

       http
        .cors(cors -> {}) // ✅ MODERN FIX
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/users/login-send-otp",
                                "/api/users/login-verify-otp",
                                "/api/users/check-and-send-otp",
                                "/api/users/verify-otp-and-register",
                                "/api/users/forgot-password",
                                "/api/users/verify-reset-otp",
                                "/api/users/reset-password"
                        ).permitAll()

                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}