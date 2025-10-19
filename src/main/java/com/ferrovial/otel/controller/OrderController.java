package com.ferrovial.otel.controller;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OpenTelemetry openTelemetry;
    private static final AttributeKey<String> ATTR_TENANT = AttributeKey.stringKey("tenant");
    private static final AttributeKey<String> ATTR_CHANNEL = AttributeKey.stringKey("channel");
    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);

    public OrderController(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    private Tracer tracer;
    private LongCounter ordersProcessed;

    @PostConstruct
    public void init() {
        tracer = openTelemetry.getTracer("orders-service");
        Meter meter = openTelemetry.meterBuilder("orders-service").build();
        ordersProcessed = meter.counterBuilder("orders.processed")
                .setDescription("Number of processed orders")
                .setUnit("1")
                .build();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest req) {

        Span span = tracer.spanBuilder("orders.create")
                .setAttribute("order.amount", req.amount())
                .setAttribute("order.currency", req.currency())
                .setAttribute("customer.id", req.customerId())
                .startSpan();
        try {
            // add a business event
            span.addEvent("order.validation.start");

            // Validation logic fails if amount <= 0
            boolean ok = req.amount() > 0;

            span.addEvent("order.validation.done", Attributes.of(
                    AttributeKey.booleanKey("valid"), ok));

            if (!ok) {
                throw new IllegalArgumentException("Amount must be > 0");
            }

            // record a custom metric with labels
            ordersProcessed.add(1, Attributes.of(
                    ATTR_TENANT, req.tenant(),
                    ATTR_CHANNEL, req.channel()));

            OrderResponse response = new OrderResponse(MDC.get("correlation_id"));
            span.setAttribute("order.status", "ACCEPTED");
            LOG.debug("Order accepted: {}", response);

            return ResponseEntity
                    .status(HttpStatus.CREATED)   // 201 Created
                    .body(response);
        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("order.status", "REJECTED");
            throw e;
        } finally {
            span.end();
        }
    }

    public record OrderRequest(String customerId, double amount, String currency,
                               String tenant, String channel) {}
    public record OrderResponse(String correlationId) {}
}

