package com.example.APIGatewayQuickBite.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter implements Ordered {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Evitamos ensuciar la consola con peticiones repetitivas de documentación
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/auth-docs")
                || path.equals("/user-docs")
                || path.equals("/menu-docs")
                || path.equals("/order-docs")
                || path.equals("/payment-docs")
                || path.equals("/notification-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Generamos un identificador de 8 caracteres único para cada flujo de petición
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();

        // LOG DE ENTRADA
        log.info("[TRACE-ID: {}] 📥 ENTRADA: [{}] {} | Origen IP: {}", traceId, method, uri, remoteAddr);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // LOG DE SALIDA (Diferencia si hay un código de error o éxito)
            if (status >= 400) {
                log.warn("[TRACE-ID: {}] ⚠️ SALIDA: [{}] {} | Estado HTTP: {} | Duración: {}ms", traceId, method, uri, status, duration);
            } else {
                log.info("[TRACE-ID: {}] 📤 SALIDA: [{}] {} | Estado HTTP: {} | Duración: {}ms", traceId, method, uri, status, duration);
            }
        }
    }

    @Override
    public int getOrder() {
        // Máxima prioridad para calcular los tiempos desde el inicio absoluto del ciclo de vida
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
