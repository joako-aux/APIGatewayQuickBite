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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. RUTAS PÚBLICAS: Únicamente la autenticación (Login/Registro)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. RUTAS PROTEGIDAS: Todo el resto del ecosistema de microservicios requiere JWT válido
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

                        // Cualquier otra ruta residual se bloquea por seguridad
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtGatewayFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}