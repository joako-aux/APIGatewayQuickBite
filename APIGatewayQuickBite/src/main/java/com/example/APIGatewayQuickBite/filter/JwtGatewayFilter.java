package com.example.APIGatewayQuickBite.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.APIGatewayQuickBite.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class JwtGatewayFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtGatewayFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // OMITIR SWAGGER: Evita que este filtro intercepte las peticiones de documentación
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/auth-docs")
                || path.startsWith("/user-docs")
                || path.startsWith("/menu-docs")
                || path.startsWith("/order-docs")
                || path.startsWith("/payment-docs")
                || path.startsWith("/notification-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        final String finalEmail;
        final String finalRole;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValid(token)) {
                finalEmail = jwtService.extractEmail(token);
                finalRole = jwtService.extractRole(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                finalEmail,
                                null,
                                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + finalRole))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            finalEmail = "user_mal_ingresado";
            finalRole = "role_mal_ingresado";
        }

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("X-User-Email".equalsIgnoreCase(name)) {
                    return finalEmail;
                }
                if ("X-User-Role".equalsIgnoreCase(name)) {
                    return finalRole;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("X-User-Email".equalsIgnoreCase(name)) {
                    return Collections.enumeration(List.of(finalEmail));
                }
                if ("X-User-Role".equalsIgnoreCase(name)) {
                    return Collections.enumeration(List.of(finalRole));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                if (!names.contains("X-User-Email")) names.add("X-User-Email");
                if (!names.contains("X-User-Role")) names.add("X-User-Role");
                return Collections.enumeration(names);
            }
        };

        filterChain.doFilter(wrappedRequest, response);
    }
}
