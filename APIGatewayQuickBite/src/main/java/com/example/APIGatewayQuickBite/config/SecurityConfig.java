package com.example.APIGatewayQuickBite.config;

import com.example.APIGatewayQuickBite.filter.JwtGatewayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtGatewayFilter jwtGatewayFilter;

    public SecurityConfig(JwtGatewayFilter jwtGatewayFilter) {
        this.jwtGatewayFilter = jwtGatewayFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // AUTH PUBLICO
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // SWAGGER GATEWAY
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // SWAGGER MICROSERVICIOS
                        .requestMatchers(
                                "/auth-docs/**",
                                "/user-docs/**",
                                "/menu-docs/**",
                                "/order-docs/**",
                                "/payment-docs/**",
                                "/notification-docs/**",
                                "/auth-docs",
                                "/user-docs",
                                "/menu-docs",
                                "/order-docs",
                                "/payment-docs",
                                "/notification-docs"
                        ).permitAll()

                        // MICROSERVICIOS PROTEGIDOS
                        .requestMatchers(
                                "/api/users/**",
                                "/api/usuarios/**",

                                "/api/menu/**",
                                "/api/categorias/**",
                                "/api/productos/**",

                                "/api/ordenes/**",

                                "/api/pagos/**",

                                "/api/notificaciones/**"
                        ).authenticated()

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