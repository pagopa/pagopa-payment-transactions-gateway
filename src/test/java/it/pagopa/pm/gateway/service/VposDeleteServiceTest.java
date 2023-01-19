package it.pagopa.pm.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pm.gateway.beans.ValidBeans;
import it.pagopa.pm.gateway.client.restapicd.RestapiCdClientImpl;
import it.pagopa.pm.gateway.client.vpos.HttpClient;
import it.pagopa.pm.gateway.dto.creditcard.StepZeroRequest;
import it.pagopa.pm.gateway.dto.enums.ThreeDS2ResponseTypeEnum;
import it.pagopa.pm.gateway.dto.vpos.AuthResponse;
import it.pagopa.pm.gateway.dto.vpos.VposDeleteResponse;
import it.pagopa.pm.gateway.dto.vpos.VposOrderStatusResponse;
import it.pagopa.pm.gateway.entity.PaymentRequestEntity;
import it.pagopa.pm.gateway.repository.PaymentRequestRepository;
import it.pagopa.pm.gateway.utils.VPosRequestUtils;
import it.pagopa.pm.gateway.utils.VPosResponseUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static it.pagopa.pm.gateway.constant.Messages.*;
import static it.pagopa.pm.gateway.dto.enums.PaymentRequestStatusEnum.CANCELLED;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = VposDeleteService.class)
public class VposDeleteServiceTest {

    @Spy
    @InjectMocks
    private VposDeleteService service = new VposDeleteService();

    @Before
    public void setUpProperties() {
        ReflectionTestUtils.setField(service, "vposUrl", "http://localhost:8080");
    }

    @Mock
    private PaymentRequestRepository paymentRequestRepository;
    @Mock
    private RestapiCdClientImpl restapiCdClient;
    @Mock
    private VPosRequestUtils vPosRequestUtils;
    @Mock
    private VPosResponseUtils vPosResponseUtils;
    @Mock
    private HttpClient httpClient;
    @Mock
    private ObjectMapper objectMapper;

    private final String UUID_SAMPLE = "8d8b30e3-de52-4f1c-a71c-9905a8043dac";

    @Test
    public void startDelete_Test_OK() throws IOException {
        StepZeroRequest stepZeroRequest = ValidBeans.createStep0Request(false);

        PaymentRequestEntity entity = new PaymentRequestEntity();
        entity.setResponseType(ThreeDS2ResponseTypeEnum.CHALLENGE.name());
        String requestJson = objectMapper.writeValueAsString(stepZeroRequest);
        entity.setJsonRequest(requestJson);
        entity.setIdTransaction("1235");
        entity.setStatus(CANCELLED.name());

        Map<String, String> params = new HashMap<>();
        params.put("1", "prova");

        VposOrderStatusResponse vposOrderStatusResponse = new VposOrderStatusResponse();
        vposOrderStatusResponse.setResultCode("00");
        AuthResponse authResponse = new AuthResponse();
        authResponse.setResultCode("00");

        when(paymentRequestRepository.findByGuid(any())).thenReturn(entity);
        when(objectMapper.readValue(entity.getJsonRequest(), StepZeroRequest.class)).thenReturn(stepZeroRequest);
        when(vPosRequestUtils.buildOrderStatusParams(any(), any())).thenReturn(params);
        when(httpClient.post(any(), any(), any())).thenReturn(ValidBeans.createHttpClientResponseVPos());
        when(vPosResponseUtils.buildOrderStatusResponse(any())).thenReturn(vposOrderStatusResponse);
        when(vPosRequestUtils.buildRevertRequestParams(any(), any())).thenReturn(params);
        when(vPosResponseUtils.buildAuthResponse(any())).thenReturn(authResponse);


        VposDeleteResponse resposeTest = ValidBeans.createVposDeleteResponse(UUID_SAMPLE, null, true);
        resposeTest.setStatus(CANCELLED.name());
        VposDeleteResponse responseService = service.startDelete(UUID_SAMPLE);

        assertEquals(resposeTest, responseService);
    }

    @Test
    public void startDelete_Test_KO_OrderStatus() throws IOException {
        StepZeroRequest stepZeroRequest = ValidBeans.createStep0Request(false);

        PaymentRequestEntity entity = new PaymentRequestEntity();
        entity.setResponseType(ThreeDS2ResponseTypeEnum.CHALLENGE.name());
        String requestJson = objectMapper.writeValueAsString(stepZeroRequest);
        entity.setJsonRequest(requestJson);
        entity.setIdTransaction("1235");

        Map<String, String> params = new HashMap<>();
        params.put("1", "prova");

        VposOrderStatusResponse vposOrderStatusResponse = new VposOrderStatusResponse();
        vposOrderStatusResponse.setResultCode("02");

        when(paymentRequestRepository.findByGuid(any())).thenReturn(entity);
        when(objectMapper.readValue(entity.getJsonRequest(), StepZeroRequest.class)).thenReturn(stepZeroRequest);
        when(vPosRequestUtils.buildOrderStatusParams(any(), any())).thenReturn(params);
        when(httpClient.post(any(), any(), any())).thenReturn(ValidBeans.createHttpClientResponseVPos());
        when(vPosResponseUtils.buildOrderStatusResponse(any())).thenReturn(vposOrderStatusResponse);


        VposDeleteResponse resposeTest = ValidBeans.createVposDeleteResponse(UUID_SAMPLE, "Error during orderStatus", false);
        VposDeleteResponse responseService = service.startDelete(UUID_SAMPLE);

        assertEquals(resposeTest, responseService);
    }

    @Test
    public void startDelete_Test_KO_Revert() throws IOException {
        StepZeroRequest stepZeroRequest = ValidBeans.createStep0Request(false);

        PaymentRequestEntity entity = new PaymentRequestEntity();
        entity.setResponseType(ThreeDS2ResponseTypeEnum.CHALLENGE.name());
        String requestJson = objectMapper.writeValueAsString(stepZeroRequest);
        entity.setJsonRequest(requestJson);
        entity.setIdTransaction("1235");

        Map<String, String> params = new HashMap<>();
        params.put("1", "prova");

        VposOrderStatusResponse vposOrderStatusResponse = new VposOrderStatusResponse();
        vposOrderStatusResponse.setResultCode("00");
        AuthResponse authResponse = new AuthResponse();
        authResponse.setResultCode("02");

        when(paymentRequestRepository.findByGuid(any())).thenReturn(entity);
        when(objectMapper.readValue(entity.getJsonRequest(), StepZeroRequest.class)).thenReturn(stepZeroRequest);
        when(vPosRequestUtils.buildOrderStatusParams(any(), any())).thenReturn(params);
        when(httpClient.post(any(), any(), any())).thenReturn(ValidBeans.createHttpClientResponseVPos());
        when(vPosResponseUtils.buildOrderStatusResponse(any())).thenReturn(vposOrderStatusResponse);
        when(vPosRequestUtils.buildRevertRequestParams(any(), any())).thenReturn(params);
        when(vPosResponseUtils.buildAuthResponse(any())).thenReturn(authResponse);


        VposDeleteResponse resposeTest = ValidBeans.createVposDeleteResponse(UUID_SAMPLE, "Error during Revert", false);
        VposDeleteResponse responseService = service.startDelete(UUID_SAMPLE);

        assertEquals(resposeTest, responseService);
    }

    @Test
    public void startDelete_Test_EntityNull() {
        VposDeleteResponse resposeTest = ValidBeans.createVposDeleteResponse(UUID_SAMPLE, REQUEST_ID_NOT_FOUND_MSG, false);
        when(paymentRequestRepository.findByGuid(any())).thenReturn(null);
        VposDeleteResponse responseService = service.startDelete(UUID_SAMPLE);
        assertEquals(resposeTest, responseService);
    }

    @Test
    public void startDelete_Test_EntityAlreadyAuthorized() {
        PaymentRequestEntity entity = new PaymentRequestEntity();
        entity.setIdTransaction("1235");
        entity.setIsRefunded(true);
        VposDeleteResponse resposeTest = ValidBeans.createVposDeleteResponse(UUID_SAMPLE, String.format(TRANSACTION_ALREADY_REFUND, entity.getIdTransaction()), false);
        when(paymentRequestRepository.findByGuid(any())).thenReturn(entity);
        VposDeleteResponse responseService = service.startDelete(UUID_SAMPLE);
        assertEquals(resposeTest, responseService);
    }

    @Test
    public void startDelete_Test_Exception_In_getStepZeroRequest() throws IOException {
        StepZeroRequest stepZeroRequest = ValidBeans.createStep0Request(false);

        PaymentRequestEntity entity = new PaymentRequestEntity();
        entity.setResponseType(ThreeDS2ResponseTypeEnum.CHALLENGE.name());
        String requestJson = objectMapper.writeValueAsString(stepZeroRequest);
        entity.setJsonRequest(requestJson);
        entity.setIdTransaction("1235");

        VposDeleteResponse resposeTest = ValidBeans.createVposDeleteResponse(UUID_SAMPLE, GENERIC_REFUND_ERROR_MSG + UUID_SAMPLE, false);

        when(paymentRequestRepository.findByGuid(any())).thenReturn(entity);
        when(objectMapper.readValue(entity.getJsonRequest(), StepZeroRequest.class)).thenThrow(new RuntimeException());

        VposDeleteResponse responseService = service.startDelete(UUID_SAMPLE);
        assertEquals(resposeTest, responseService);
    }

    @Test
    public void startDelete_Test_Error_In_CallVpos() throws IOException {
        StepZeroRequest stepZeroRequest = ValidBeans.createStep0Request(false);

        PaymentRequestEntity entity = new PaymentRequestEntity();
        entity.setResponseType(ThreeDS2ResponseTypeEnum.CHALLENGE.name());
        String requestJson = objectMapper.writeValueAsString(stepZeroRequest);
        entity.setJsonRequest(requestJson);
        entity.setIdTransaction("1235");

        Map<String, String> params = new HashMap<>();
        params.put("1", "prova");

        when(paymentRequestRepository.findByGuid(any())).thenReturn(entity);
        when(objectMapper.readValue(entity.getJsonRequest(), StepZeroRequest.class)).thenReturn(stepZeroRequest);
        when(vPosRequestUtils.buildOrderStatusParams(any(), any())).thenReturn(params);
        when(httpClient.post(any(), any(), any())).thenReturn(ValidBeans.createKOHttpClientResponseVPos());


        VposDeleteResponse resposeTest = ValidBeans.createVposDeleteResponse(UUID_SAMPLE, GENERIC_REFUND_ERROR_MSG + UUID_SAMPLE, false);
        VposDeleteResponse responseService = service.startDelete(UUID_SAMPLE);

        assertEquals(resposeTest, responseService);
    }

}
