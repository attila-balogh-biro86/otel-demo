package com.ferrovial.otel.config;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtelConfig {

    private static final String CONNECTION_STRING = "InstrumentationKey=8d1db4ec-93b0-4ceb-8629-8188a58e5b4b;IngestionEndpoint=https://westeurope-5.in.applicationinsights.azure.com/;LiveEndpoint=https://westeurope.livediagnostics.monitor.azure.com/;ApplicationId=664a61c4-291e-4a79-97a6-6c29ee9bcada";

    @Bean
    public OpenTelemetry getOpenTelemetry() {
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        AzureMonitorAutoConfigure.customize(sdkBuilder, CONNECTION_STRING);
        return sdkBuilder.build().getOpenTelemetrySdk();
    }
}
