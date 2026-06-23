package com.example.APIGatewayQuickBite.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Set;

@Configuration
public class GatewaySwaggerConfig {

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties(SwaggerUiConfigProperties properties) {
        // Configuramos manualmente las agrupaciones que alimentarán el menú desplegable (dropdown)
        properties.setUrls(Set.of(
                new SwaggerUrl("1. Autenticación", "/auth-docs", "Auth Service"),
                new SwaggerUrl("2. Usuarios", "/user-docs", "User Service"),
                new SwaggerUrl("3. Menú", "/menu-docs", "Menu Service"),
                new SwaggerUrl("4. Órdenes", "/order-docs", "Order Service"),
                new SwaggerUrl("5. Pagos", "/payment-docs", "Payment Service"),
                new SwaggerUrl("6. Notificaciones", "/notification-docs", "Notification Service")
        ));
        return properties;
    }
}
