package com.example.APIGatewayQuickBite.config; // 👈 Este es el paquete de configuración

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component // 👈 Al estar en el paquete config con esta anotación, Spring lo levanta solo
public class GatewayForwardInterceptor implements RestClientCustomizer {

    @Override
    public void customize(RestClient.Builder restClientBuilder) {
        restClientBuilder.requestInterceptor((request, body, execution) -> {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();

                String role = authentication.getAuthorities().stream()
                        .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                        .findFirst()
                        .orElse("CLIENTE");

                // Inyección física en las cabeceras HTTP de red
                request.getHeaders().add("X-User-Email", email);
                request.getHeaders().add("X-User-Role", role);

                System.out.println("GATEWAY ENVIANDO ENRUTAMIENTO -> Email: " + email + " | Rol: " + role);
            }

            return execution.execute(request, body);
        });
    }
}