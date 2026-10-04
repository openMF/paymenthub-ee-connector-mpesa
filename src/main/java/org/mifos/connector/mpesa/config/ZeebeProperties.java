package org.mifos.connector.mpesa.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Where the Zeebe broker is, how many threads talk to it, and the two timers this connector uses.
 *
 * <p>
 * {@code zeebe.client.evenly-allocated-max-jobs} is deliberately not here: it is a SpEL expression in application.yml
 * ({@code "#{${zeebe.client.max-execution-threads} / ${zeebe.client.number-of-workers}}"}) and only {@code @Value} evaluates SpEL. Same for
 * {@code zeebe.client.number-of-workers}, which exists only to feed that expression.
 * </p>
 *
 * <p>
 * Every value is required, as it was when it was a bare {@code @Value} field.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "zeebe")
public record ZeebeProperties(@NotNull @Valid Broker broker, @NotNull @Valid Client client, @NotNull @Valid InitTransfer initTransfer) {

    public record Broker(@NotNull String contactpoint) {}

    /** {@code ttl} is how long a published Zeebe message stays correlatable, in milliseconds. */
    public record Client(@NotNull Integer maxExecutionThreads, @NotNull Integer ttl) {}

    /** How long the init-transfer worker waits before calling Safaricom, in seconds. */
    public record InitTransfer(@NotNull Integer waitTimer) {}
}
