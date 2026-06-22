package com.example.APIGatewayQuickBite.filter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.APIGatewayQuickBite.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtGatewayFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtGatewayFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Permitir rutas públicas
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        System.out.println("EMAIL: " + email);
        System.out.println("ROLE: " + role);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        java.util.Collections.emptyList()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(
                request,
                response
        );

        filterChain.doFilter(request, response);
    }
}
