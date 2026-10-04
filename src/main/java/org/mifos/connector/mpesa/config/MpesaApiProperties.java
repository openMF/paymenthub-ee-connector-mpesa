package org.mifos.connector.mpesa.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The Safaricom endpoints this connector calls, and the callback address it asks Safaricom to call back on.
 *
 * <p>
 * Named {@code MpesaApiProperties} rather than {@code MpesaProperties} because {@code MpesaProps} already exists and binds a different
 * prefix ({@code accounts}), which is confusing enough without a third similar name.
 * </p>
 *
 * <p>
 * Every value is required, as it was when it was a bare {@code @Value} field.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "mpesa")
public record MpesaApiProperties(@NotNull Integer maxRetryCount, @NotNull @Valid Api api, @NotNull @Valid Local local) {

    /** {@code timeout} is applied to every outgoing Safaricom call, in milliseconds. */
    public record Api(@NotNull Integer timeout, @NotNull String lipana, @NotNull String transactionStatus) {}

    public record Local(@NotNull String host, @NotNull String transactionCallback) {}
}
