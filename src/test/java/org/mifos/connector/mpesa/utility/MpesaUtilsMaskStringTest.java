package org.mifos.connector.mpesa.utility;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The Safaricom access token used to be written to the log in clear by {@code AuthRoutes}. It now goes through this, which already existed
 * and was already used a few routes away - so these tests are about the one property that matters: the secret does not come back out.
 */
class MpesaUtilsMaskStringTest {

    /** A real token from the pod's log, which is how this was found. */
    private static final String REAL_TOKEN = "WEAwJ6YWMNtQsvBpSGjTyislwjNG";

    @Test
    @DisplayName("everything but the last four characters is masked")
    void masksAllButTheLastFour() {
        String masked = MpesaUtils.maskString(REAL_TOKEN);

        assertThat(masked).hasSameSizeAs(REAL_TOKEN).endsWith("wjNG").doesNotContain("WEAwJ6YWMNtQsvBpSGjTyisl");
        assertThat(masked.substring(0, masked.length() - 4)).matches("[*]+");
    }

    @Test
    @DisplayName("a short value is not partly revealed")
    void shortValuesAreFullyMasked() {
        assertThat(MpesaUtils.maskString("abc")).isEqualTo("***");
        assertThat(MpesaUtils.maskString("")).isEqualTo("***");
    }

    @Test
    @DisplayName("a null token does not throw, which is what made this risky to call from a route")
    void nullIsHandled() {
        assertThat(MpesaUtils.maskString(null)).isEqualTo("***");
    }
}
