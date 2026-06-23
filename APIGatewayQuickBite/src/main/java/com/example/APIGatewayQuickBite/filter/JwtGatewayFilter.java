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

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // Variables finales que usará nuestro Wrapper
        final String finalEmail;
        final String finalRole;

        // Evaluamos si viene un token válido
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValid(token)) {
                finalEmail = jwtService.extractEmail(token);
                finalRole = jwtService.extractRole(token);

                // Configurar el contexto de seguridad local del Gateway
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                finalEmail,
                                null,
                                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + finalRole))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Si el token viene pero es inválido/expirado, rechazamos con 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            // CASO NEGATIVO: Si no viene token y la ruta es protegida, Spring Security se encargará de rebotarla.
            // Si la ruta es pública (como /profile), permitimos que pase inyectando los valores por defecto.
            finalEmail = "user_mal_ingresado";
            finalRole = "role_mal_ingresado";
        }

        // MUTACIÓN: Envolvemos la petición para inyectar físicamente las cabeceras HTTP (reales o por defecto)
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

        // Pasamos la petición ENVUELTA al resto de la cadena (incluyendo Spring Security)
        filterChain.doFilter(wrappedRequest, response);
    }
}