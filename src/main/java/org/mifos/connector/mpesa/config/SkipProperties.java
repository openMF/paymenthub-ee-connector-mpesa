package org.mifos.connector.mpesa.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * When enabled, the workers pretend Safaricom answered instead of calling it. Used for demos and local runs. Required,
 * as it was when it was a bare {@code @Value} field.
 */
@Validated
@ConfigurationProperties(prefix = "skip")
public record SkipProperties(@NotNull Boolean enabled) {}
