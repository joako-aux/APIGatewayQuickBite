package com.example.APIGatewayQuickBite.config;

import com.example.APIGatewayQuickBite.filter.JwtGatewayFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtGatewayFilter jwtGatewayFilter;

    public SecurityConfig(
            JwtGatewayFilter jwtGatewayFilter
    ) {
        this.jwtGatewayFilter = jwtGatewayFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth ->
                        auth

                                .requestMatchers(
                                        "/api/auth/**"
                                )
                                .permitAll()

                                .anyRequest()
                                .authenticated()
                )

                .addFilterBefore(
                        jwtGatewayFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
