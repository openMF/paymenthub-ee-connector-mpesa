package org.mifos.connector.mpesa.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * Binds every properties record the way the running pod does, from the exact names it uses.
 *
 * <p>
 * Moving a property from {@code @Value} to {@code @ConfigurationProperties} changes how a name resolves, so every name is pinned here. On
 * this connector the deployment sets almost nothing - only {@code PORT} and {@code ZEEBE_BROKER_CONTACTPOINT} are in the pod's environment
 * - which means the committed application.yml is the live configuration and a silent binding change would go unnoticed for a long time.
 * </p>
 */
class DeploymentEnvironmentBindingTest {

    /** Exactly what {@code kubectl get pod ... -o json} shows on the running paymenthub-ee-connector-mpesa pod. */
    private static final Map<String, Object> DEPLOYMENT_ENVIRONMENT = Map.of("PORT", "5000", "ZEEBE_BROKER_CONTACTPOINT",
            "paymenthub-infra-zeebe-gateway:26500");

    /** The application.yml values, which on this connector are what actually runs. */
    private static Map<String, Object> fileDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("zeebe.broker.contactpoint", "localhost:26500");
        defaults.put("zeebe.client.max-execution-threads", 100);
        defaults.put("zeebe.client.ttl", 30000);
        defaults.put("zeebe.init-transfer.wait-timer", 5);
        defaults.put("mpesa.max-retry-count", 2);
        defaults.put("mpesa.api.timeout", 60000);
        defaults.put("mpesa.api.lipana", "/mpesa/stkpush/v1/processrequest");
        defaults.put("mpesa.api.transaction-status", "/mpesa/stkpushquery/v1/query");
        defaults.put("mpesa.local.host", "http://localhost:5000");
        defaults.put("mpesa.local.transaction-callback", "/buygoods/callback");
        defaults.put("operations.host", "http://paymenthub-ee-bff:80");
        defaults.put("operations.base-url", "/api/v1/errorcode");
        defaults.put("operations.filter-path", "/filter");
        defaults.put("skip.enabled", false);
        return defaults;
    }

    /** Binds the way the application does: environment first, file defaults behind it. */
    private static <T> T bind(Map<String, Object> environmentVariables, String prefix, Class<T> type) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, environmentVariables));
        environment.getPropertySources().addLast(new MapPropertySource("file-defaults", fileDefaults()));

        return new Binder(ConfigurationPropertySources.get(environment)).bind(prefix, Bindable.of(type)).get();
    }

    @Test
    @DisplayName("ZEEBE_BROKER_CONTACTPOINT from the deployment wins over the localhost default in the file")
    void zeebeContactpointComesFromTheDeployment() {
        ZeebeProperties properties = bind(DEPLOYMENT_ENVIRONMENT, "zeebe", ZeebeProperties.class);

        assertThat(properties.broker().contactpoint()).isEqualTo("paymenthub-infra-zeebe-gateway:26500");
        // not set by the deployment, so these have to come from application.yml
        assertThat(properties.client().maxExecutionThreads()).isEqualTo(100);
        assertThat(properties.client().ttl()).isEqualTo(30000);
        assertThat(properties.initTransfer().waitTimer()).isEqualTo(5);
    }

    @Test
    @DisplayName("the Safaricom endpoints and the callback address bind from the file")
    void mpesaApiPropertiesBind() {
        MpesaApiProperties properties = bind(DEPLOYMENT_ENVIRONMENT, "mpesa", MpesaApiProperties.class);

        assertThat(properties.maxRetryCount()).isEqualTo(2);
        assertThat(properties.api().timeout()).isEqualTo(60000);
        assertThat(properties.api().lipana()).isEqualTo("/mpesa/stkpush/v1/processrequest");
        assertThat(properties.api().transactionStatus()).isEqualTo("/mpesa/stkpushquery/v1/query");
        assertThat(properties.local().host()).isEqualTo("http://localhost:5000");
        assertThat(properties.local().transactionCallback()).isEqualTo("/buygoods/callback");
    }

    @Test
    @DisplayName("the operations URL is assembled from the same three parts as before")
    void operationsFilterUrlIsUnchanged() {
        OperationsApiProperties properties = bind(DEPLOYMENT_ENVIRONMENT, "operations", OperationsApiProperties.class);

        assertThat(properties.filterUrl()).isEqualTo("http://paymenthub-ee-bff:80/api/v1/errorcode/filter");
    }

    @Test
    @DisplayName("skip.enabled binds from the file, and the environment can switch it on")
    void skipPropertiesBind() {
        assertThat(bind(DEPLOYMENT_ENVIRONMENT, "skip", SkipProperties.class).enabled()).isFalse();
        assertThat(bind(Map.of("SKIP_ENABLED", "true"), "skip", SkipProperties.class).enabled()).isTrue();
    }

    @Test
    @DisplayName("a timeout that is not a number is refused at binding time, naming the property")
    void malformedTimeoutIsRefused() {
        assertThatThrownBy(() -> bind(Map.of("MPESA_API_TIMEOUT", "one minute"), "mpesa", MpesaApiProperties.class))
                .hasStackTraceContaining("mpesa.api.timeout");
    }
}
