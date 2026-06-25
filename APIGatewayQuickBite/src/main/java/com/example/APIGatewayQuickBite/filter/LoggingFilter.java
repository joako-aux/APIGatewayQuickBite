package com.example.APIGatewayQuickBite.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

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
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String traceId =
                UUID.randomUUID().toString().substring(0, 8);

        long start = System.currentTimeMillis();

        log.info(
                "[TRACE-ID: {}] ENTRADA [{}] {}",
                traceId,
                request.getMethod(),
                request.getRequestURI()
        );

        filterChain.doFilter(request, response);

        long duration =
                System.currentTimeMillis() - start;

        log.info(
                "[TRACE-ID: {}] SALIDA [{}] {} -> {} ({} ms)",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration
        );
    }
}