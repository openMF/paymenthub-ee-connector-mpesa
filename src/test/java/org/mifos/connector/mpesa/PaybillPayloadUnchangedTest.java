package org.mifos.connector.mpesa;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mifos.connector.common.gsma.dto.GsmaTransfer;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillResponseDTO;
import org.mifos.connector.mpesa.utility.MpesaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * PaybillRoute used to serialise its outgoing payloads with a private {@code new ObjectMapper()} instead of the configured bean. Swapping
 * that for the bean is only safe if the bytes on the wire do not move, because the channel connector parses them - so this pins them.
 */
@SpringBootTest
class PaybillPayloadUnchangedTest {

    @Autowired
    private ObjectMapper configuredMapper;

    @Autowired
    private MpesaUtils mpesaUtils;

    private final ObjectMapper oldPrivateMapper = new ObjectMapper();

    private static PaybillResponseDTO paybillResponse() {
        PaybillResponseDTO dto = new PaybillResponseDTO();
        dto.setTransactionId("TXN-1");
        dto.setMsisdn("254708374149");
        dto.setAmount("100");
        dto.setCurrency("KES");
        dto.setAmsName("roster");
        dto.setAccountHoldingInstitutionId("default");
        dto.setReconciled(true);
        return dto;
    }

    @Test
    @DisplayName("the GSMA transfer sent to the channel connector serialises the same as before")
    void gsmaTransferPayloadIsUnchanged() throws Exception {
        GsmaTransfer transfer = mpesaUtils.createGsmaTransferDTO(paybillResponse(), "TXN-1");

        assertThat(configuredMapper.writeValueAsString(transfer)).isEqualTo(oldPrivateMapper.writeValueAsString(transfer));
    }

    @Test
    @DisplayName("the channel request built from a paybill validation serialises the same as before")
    void channelRequestPayloadIsUnchanged() throws Exception {
        PaybillRequestDTO request = new PaybillRequestDTO();
        request.setTransactionID("TXN-1");
        request.setMsisdn("254708374149");
        request.setBillRefNo("ACC-1");
        request.setTransactionAmount("100");
        request.setShortCode("12345678");

        Object payload = MpesaUtils.convertPaybillPayloadToChannelPayload(request, "roster", "KES");

        assertThat(configuredMapper.writeValueAsString(payload)).isEqualTo(oldPrivateMapper.writeValueAsString(payload));
    }
}
