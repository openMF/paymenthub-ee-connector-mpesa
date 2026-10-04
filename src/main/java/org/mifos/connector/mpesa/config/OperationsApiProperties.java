package org.mifos.connector.mpesa.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The operations API this connector asks whether a Safaricom error code is worth retrying.
 *
 * <p>
 * Named {@code OperationsApiProperties} because {@code camel.config.OperationsProperties} already exists and holds query-parameter
 * constants, not configuration.
 * </p>
 *
 * <p>
 * Every value is required, as it was when it was a bare {@code @Value} field.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "operations")
public record OperationsApiProperties(@NotNull String host, @NotNull String baseUrl, @NotNull String filterPath) {

    /** The full URL the error-code routes call. */
    public String filterUrl() {
        return host + baseUrl + filterPath;
    }
}
