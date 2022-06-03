package it.pagopa.pm.gateway.client.restapicd;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import it.pagopa.pm.gateway.dto.TransactionUpdateRequest;
import it.pagopa.pm.gateway.dto.TransactionUpdateRequestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

import static it.pagopa.pm.gateway.utils.MdcUtils.buildMdcHeader;

@Slf4j
@Component
public class RestapiCdClientImpl {

    private static final String OUTCOME_PARAM = "outcome";
    private static final String AUTH_CODE_PARAM = "authCode";
    @Value("${HOSTNAME_PM}")
    public String hostnamePm;

    @PostConstruct
    public void init() {
        restapiCdClient = Feign.builder().client(new OkHttpClient()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder()).target(RestapiCdClient.class, hostnamePm);
    }

    private RestapiCdClient restapiCdClient;

    public void callTransactionUpdate(Long id, TransactionUpdateRequest request) {
        log.info("Calling PATCH to update transaction " + id);
        Map<String, Object> headerMap = buildMdcHeader();
        restapiCdClient.updateTransaction(id, headerMap, new TransactionUpdateRequestData(request));
    }

    public String callClosePayment(Long idTransaction, boolean outcome, String authCode) {
        log.info("Calling Payment Manager's closePayment for transaction " + idTransaction);
        Map<String, Object> headerMap = buildMdcHeader();
        Map<String, Object> parameters = buildQueryParameters(outcome, authCode);
        return restapiCdClient.closePayment(idTransaction, parameters, headerMap);
    }

    private Map<String, Object> buildQueryParameters(boolean outcome, String authCode) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put(OUTCOME_PARAM, outcome);
        parameters.put(AUTH_CODE_PARAM, authCode);
        return parameters;
    }

}
