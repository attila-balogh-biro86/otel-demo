package com.ferrovial.otel.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String corr = Optional.ofNullable(request.getHeader("X-Correlation-Id"))
                .orElse(UUID.randomUUID().toString());
        MDC.put("correlation_id", corr);
        response.setHeader("X-Correlation-Id", corr);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
