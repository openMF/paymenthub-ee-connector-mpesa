package org.mifos.connector.mpesa.flowcomponents.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

class MpesaWorkerBuyGoodsResultTest {

    private final Exchange exchange = new DefaultExchange(new DefaultCamelContext());
    private final Map<String, Object> variables = new HashMap<>();

    @Test
    void aSuccessfulPaymentIsReportedAsMade() {
        exchange.setProperty("transactionFailed", false);
        exchange.setProperty("mpesaTransactionId", "ws_CO_123");

        MpesaWorker.applyBuyGoodsResult(exchange, variables);

        assertThat(variables).containsEntry("transactionFailed", false).containsEntry("mpesaTransactionId", "ws_CO_123");
    }

    @Test
    void aRouteThatThrewIsReportedAsFailed() {
        exchange.setException(new IllegalStateException("access token request refused"));

        MpesaWorker.applyBuyGoodsResult(exchange, variables);

        assertThat(variables).containsEntry("transactionFailed", true).containsEntry("transferCreateFailed", true)
                .containsEntry("errorInformation", "access token request refused");
    }

    @Test
    void aRejectedPaymentIsReportedAsFailed() {
        exchange.setProperty("transactionFailed", true);
        exchange.setProperty("errorInformation", "{\"errorCode\":\"500.001.1001\"}");

        MpesaWorker.applyBuyGoodsResult(exchange, variables);

        assertThat(variables).containsEntry("transactionFailed", true).containsEntry("errorInformation", "{\"errorCode\":\"500.001.1001\"}");
    }
}
