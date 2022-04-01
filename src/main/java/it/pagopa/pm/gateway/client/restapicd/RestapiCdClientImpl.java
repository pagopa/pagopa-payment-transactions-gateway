package it.pagopa.pm.gateway.client.restapicd;

import feign.*;
import feign.jackson.*;
import feign.okhttp.*;
import it.pagopa.pm.gateway.dto.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;

@Slf4j
@Component
public class RestapiCdClientImpl {

    @Value("${HOSTNAME_PM}")
    public String hostnamePm;

    @PostConstruct
    public void init() {
        restapiCdClient = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(RestapiCdClient.class, hostnamePm);
    }

    private RestapiCdClient restapiCdClient;

    public void callTransactionUpdate(Long id, TransactionUpdateRequest request) {
        log.info("Calling PATCH to update transaction " + id);
        restapiCdClient.updateTransaction(id, new TransactionUpdateRequestData(request));
    }

}
