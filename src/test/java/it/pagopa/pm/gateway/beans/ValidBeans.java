package it.pagopa.pm.gateway.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pm.gateway.client.bpay.generated.*;
import it.pagopa.pm.gateway.dto.*;
import it.pagopa.pm.gateway.dto.enums.OutcomeEnum;
import it.pagopa.pm.gateway.dto.microsoft.azure.login.MicrosoftAzureLoginResponse;
import it.pagopa.pm.gateway.entity.BPayPaymentResponseEntity;
import it.pagopa.pm.gateway.entity.PaymentRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.client.model.CreatePaymentRequest;
import org.openapitools.client.model.CreatePaymentResponse;
import org.openapitools.client.model.Esito;
import org.openapitools.client.model.PaymentChannel;
import org.openapitools.client.model.ResponseURLs;
import org.openapitools.client.model.DetailsPaymentResponse;
import org.openapitools.client.model.RefundPaymentResponse;
import org.openapitools.client.model.RefundPaymentRequest;
import org.openapitools.client.model.EsitoStorno;
import org.openapitools.client.model.DetailsPaymentRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.openapitools.client.model.AuthorizationType.IMMEDIATA;

public class ValidBeans {
    private static final String EURO_ISO_CODE = "978";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String RESPONSE_URL_OK_KO = "https://portal.test.pagopa.gov.it/pmmockserviceapi/home";
    public static final String NOTIFICATION_URL = "${postepay.notificationURL}";

    public static BPayPaymentRequest bPayPaymentRequest() {
        BPayPaymentRequest request = new BPayPaymentRequest();
        request.setIdPsp("Id_psp");
        request.setIdPagoPa(1L);
        request.setAmount(100d);
        request.setSubject("causale");
        request.setEncryptedTelephoneNumber("pqimx8en49fbf");
        request.setLanguage("IT");
        return request;
    }

    public static InserimentoRichiestaPagamentoPagoPaResponse inserimentoRichiestaPagamentoPagoPaResponse() {
        InserimentoRichiestaPagamentoPagoPaResponse response = new InserimentoRichiestaPagamentoPagoPaResponse();
        ResponseInserimentoRichiestaPagamentoPagoPaVO responseData = new ResponseInserimentoRichiestaPagamentoPagoPaVO();
        responseData.setCorrelationId("id");
        EsitoVO esitoVO = new EsitoVO();
        esitoVO.setEsito(true);
        esitoVO.setCodice("0");
        esitoVO.setMessaggio("messaggio");
        responseData.setEsito(esitoVO);
        ContestoVO contestoVO = new ContestoVO();
        contestoVO.setGuid("7c7c201a-c198-31c7-864c-21d61b2bd810");
        responseData.setContesto(contestoVO);
        response.setReturn(responseData);
        return response;
    }

    public static BPayOutcomeResponse bPayPaymentOutcomeResponseToReturn() {
        BPayOutcomeResponse response = new BPayOutcomeResponse();
        response.setOutcome(true);
        return response;
    }

    public static BPayPaymentResponseEntity bPayPaymentResponseEntityToSave() {
        BPayPaymentResponseEntity entity = new BPayPaymentResponseEntity();
        entity.setIdPagoPa(1L);
        entity.setOutcome(true);
        entity.setCorrelationId("id");
        entity.setMessage("messaggio");
        entity.setErrorCode("0");
        entity.setClientGuid("8d8b30e3-de52-4f1c-a71c-9905a8043dac");
        return entity;
    }

    public static BPayPaymentResponseEntity bPayPaymentResponseEntityToSave_2() {
        BPayPaymentResponseEntity entity = new BPayPaymentResponseEntity();
        entity.setIdPagoPa(1L);
        entity.setOutcome(true);
        entity.setCorrelationId("id");
        entity.setMessage("messaggio");
        entity.setErrorCode("0");
        entity.setClientGuid("client-guid");
        entity.setIsProcessed(true);
        return entity;
    }


    public static BPayPaymentResponseEntity bPayPaymentResponseEntityToFind() {
        BPayPaymentResponseEntity entity = new BPayPaymentResponseEntity();
        entity.setIdPagoPa(1L);
        entity.setOutcome(true);
        entity.setCorrelationId("id");
        entity.setMessage("messaggio");
        entity.setErrorCode("0");
        entity.setClientGuid("client-guid");
        return entity;
    }

    public static AuthMessage authMessage(OutcomeEnum outcomeEnum) {
        AuthMessage authMessage = new AuthMessage();
        authMessage.setAuthCode("authCode");
        authMessage.setAuthOutcome(outcomeEnum);
        return authMessage;
    }

    public static ACKMessage ackMessageResponse(OutcomeEnum outcomeEnum) {
        ACKMessage ackMessage = new ACKMessage();
        ackMessage.setOutcome(outcomeEnum);
        return ackMessage;
    }

    public static TransactionUpdateRequest transactionUpdateRequest() {
        TransactionUpdateRequest transactionUpdateRequest = new TransactionUpdateRequest();
        transactionUpdateRequest.setStatus(21L);
        transactionUpdateRequest.setAuthCode("authCode");
        return transactionUpdateRequest;
    }

    public static BPayRefundRequest bPayRefundRequest() {
        BPayRefundRequest bPayRefundRequest = new BPayRefundRequest();
        bPayRefundRequest.setIdPagoPa(1L);
        bPayRefundRequest.setSubject(null);
        bPayRefundRequest.setLanguage("IT");
        bPayRefundRequest.setRefundAttempt(0);

        return bPayRefundRequest;

    }

    public static InquiryTransactionStatusResponse inquiryTransactionStatusResponse(boolean hasReturn) {
        InquiryTransactionStatusResponse inquiryTransactionStatusResponse = new InquiryTransactionStatusResponse();
        ResponseInquiryTransactionStatusVO responseInquiryTransactionStatusVO = new ResponseInquiryTransactionStatusVO();
        responseInquiryTransactionStatusVO.setEsitoPagamento("EFF");
        if (hasReturn)
            inquiryTransactionStatusResponse.setReturn(responseInquiryTransactionStatusVO);

        return inquiryTransactionStatusResponse;

    }

    public static StornoPagamentoResponse stornoPagamentoResponse(boolean hasReturn, boolean esito) {
        StornoPagamentoResponse stornoPagamentoResponse = new StornoPagamentoResponse();
        ResponseStornoPagamentoVO responseStornoPagamentoVO = new ResponseStornoPagamentoVO();
        EsitoVO esitoVO = new EsitoVO();
        esitoVO.setEsito(esito);
        responseStornoPagamentoVO.setEsito(esitoVO);
        if (hasReturn)
            stornoPagamentoResponse.setReturn(responseStornoPagamentoVO);

        return stornoPagamentoResponse;
    }

    public static CreatePaymentRequest createPaymentRequest(PaymentChannel paymentChannel) {
        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest();
        String shopId = paymentChannel.equals(PaymentChannel.APP) ? "shopIdTmp_APP" : "shopIdTmp_WEB";
        createPaymentRequest.setMerchantId("merchantId");
        createPaymentRequest.setShopId(shopId);
        createPaymentRequest.setShopTransactionId("1");
        createPaymentRequest.setAmount("1234");
        createPaymentRequest.setDescription("Pagamento bollo auto");
        createPaymentRequest.setCurrency(EURO_ISO_CODE);
        createPaymentRequest.setBuyerName("Mario Rossi");
        createPaymentRequest.setBuyerEmail("mario.rossi@gmail.com");
        createPaymentRequest.setPaymentChannel(paymentChannel);
        createPaymentRequest.setAuthType(IMMEDIATA);

        ResponseURLs responseUrls = new ResponseURLs();
        String urlOkKo = paymentChannel.equals(PaymentChannel.APP) ? StringUtils.EMPTY : RESPONSE_URL_OK_KO;
        responseUrls.setResponseUrlKo(urlOkKo);
        responseUrls.setResponseUrlOk(urlOkKo);
        responseUrls.setServerNotificationUrl(NOTIFICATION_URL);

        createPaymentRequest.setResponseURLs(responseUrls);
        return createPaymentRequest;
    }


    public static PostePayAuthRequest postePayAuthRequest(boolean isValid) {
        PostePayAuthRequest postePayAuthRequest = new PostePayAuthRequest();
        postePayAuthRequest.setGrandTotal(1234);
        postePayAuthRequest.setIdTransaction(isValid ? "1" : null);
        postePayAuthRequest.setName("Mario Rossi");
        postePayAuthRequest.setEmailNotice("mario.rossi@gmail.com");
        postePayAuthRequest.setDescription("Pagamento bollo auto");
        return postePayAuthRequest;
    }

    public static MicrosoftAzureLoginResponse microsoftAzureLoginResponse() {
        MicrosoftAzureLoginResponse microsoftAzureLoginResponse = new MicrosoftAzureLoginResponse();
        microsoftAzureLoginResponse.setAccess_token("access_token");
        microsoftAzureLoginResponse.setExpires_in(100);
        microsoftAzureLoginResponse.setToken_type("TOKEN_TYPE");
        microsoftAzureLoginResponse.setExt_expires_in(100);
        return microsoftAzureLoginResponse;

    }

    public static PostePayAuthResponse postePayAuthResponse(String clientId, boolean isError, String errorMessage) {
        PostePayAuthResponse postePayAuthResponse = new PostePayAuthResponse();
        postePayAuthResponse.setChannel(clientId);
        if (isError) {
            postePayAuthResponse.setError(errorMessage);
        } else {
            postePayAuthResponse.setUrlRedirect("${postepay.pgs.response.urlredirect}");
        }

        return postePayAuthResponse;

    }

    public static CreatePaymentResponse getOkResponse() {
        CreatePaymentResponse inlineResponse200 = new CreatePaymentResponse();
        inlineResponse200.setPaymentID("1234");
        inlineResponse200.setUserRedirectURL("www.userRedirectUrl.com");
        return inlineResponse200;
    }


    public static PaymentRequestEntity paymentRequestEntity(PostePayAuthRequest postePayAuthRequest, Boolean authorizationOutcome, String clientId) {
        String authRequestJson = null;


        if (Objects.nonNull(postePayAuthRequest)) {
            try {
                authRequestJson = OBJECT_MAPPER.writeValueAsString(postePayAuthRequest);
            } catch (JsonProcessingException jspe) {
                jspe.printStackTrace();
            }
        }
        PaymentRequestEntity paymentRequestEntity = new PaymentRequestEntity();
        paymentRequestEntity.setJsonRequest(authRequestJson);
        paymentRequestEntity.setAuthorizationUrl("www.userRedirectUrl.com");
        paymentRequestEntity.setAuthorizationOutcome(authorizationOutcome);
        paymentRequestEntity.setIsProcessed(false);
        paymentRequestEntity.setCorrelationId("1234");
        paymentRequestEntity.setAuthorizationCode(null);
        paymentRequestEntity.setIdTransaction("1");
        paymentRequestEntity.setGuid("8d8b30e3-de52-4f1c-a71c-9905a8043dac");
        paymentRequestEntity.setId(null);
        paymentRequestEntity.setClientId(clientId);
        paymentRequestEntity.setMdcInfo(null);
        paymentRequestEntity.setResourcePath(null);
        paymentRequestEntity.setRequestEndpoint("/request-payments/postepay");
        paymentRequestEntity.setResourcePath("${postepay.logo.url}");
        paymentRequestEntity.setIsOnboarding(false);
        return paymentRequestEntity;
 }

    public static PaymentRequestEntity paymentRequestEntityOnboardingFalse(PostePayAuthRequest postePayAuthRequest, Boolean authorizationOutcome, String clientId) {
        PaymentRequestEntity  paymentRequestEntity = paymentRequestEntity(postePayAuthRequest, authorizationOutcome, clientId);
        paymentRequestEntity.setIsOnboarding(false);
        return paymentRequestEntity;
    }

        public static PaymentRequestEntity paymentRequestEntityWithRefundData(String clientId, String authorizationCode, Boolean isRefunded, Boolean changeRequestEndPoint) {
        PaymentRequestEntity paymentRequestEntity = paymentRequestEntity(null, true, clientId);
        paymentRequestEntity.setAuthorizationCode(authorizationCode);
        paymentRequestEntity.setIsRefunded(isRefunded);
        if (changeRequestEndPoint){
            paymentRequestEntity.setRequestEndpoint("/invalidEndpoint");
        }
        return paymentRequestEntity;

    }

    public static PostePayPollingResponse postePayPollingResponse() {
        PostePayPollingResponse postePayPollingResponse = new PostePayPollingResponse();
        postePayPollingResponse.setChannel(PaymentChannel.APP.getValue());
        postePayPollingResponse.setUrlRedirect("www.userRedirectUrl.com");
        postePayPollingResponse.setAuthOutcome(OutcomeEnum.OK);
        postePayPollingResponse.setClientResponseUrl("www.clientResponseUrl.com");
        postePayPollingResponse.setError(StringUtils.EMPTY);
        postePayPollingResponse.setLogoResourcePath("${postepay.logo.url}");
        return postePayPollingResponse;
    }


    public static PostePayPollingResponse postePayPollingResponseError(String error, OutcomeEnum outcomeEnum) {
        PostePayPollingResponse postePayPollingResponse = new PostePayPollingResponse();
        postePayPollingResponse.setChannel(PaymentChannel.APP.getValue());
        postePayPollingResponse.setUrlRedirect("www.userRedirectUrl.com");
        postePayPollingResponse.setAuthOutcome(outcomeEnum);
        postePayPollingResponse.setClientResponseUrl(null);
        postePayPollingResponse.setError(error);
        return postePayPollingResponse;
    }


    public static ResponseEntity<PostePayRefundResponse> postePayRefundResponseResponseEntity(String requestId, String paymentId, String refundOutcome){

        PostePayRefundResponse postePayRefundResponse = new PostePayRefundResponse();
        postePayRefundResponse.setRequestId(requestId);
        postePayRefundResponse.setPaymentId(paymentId);
        postePayRefundResponse.setRefundOutcome(refundOutcome);

        return ResponseEntity.status(HttpStatus.OK).body(postePayRefundResponse);

    }

    public static PostePayRefundResponse postePayRefundResponse(String requestId, String paymentId, String refundOutcome, String error){
        PostePayRefundResponse postePayRefundResponse = new PostePayRefundResponse();
        postePayRefundResponse.setRequestId(requestId);
        postePayRefundResponse.setPaymentId(paymentId);
        postePayRefundResponse.setRefundOutcome(refundOutcome);
        postePayRefundResponse.setError(error);

        return postePayRefundResponse;

    }

    public static DetailsPaymentResponse detailsPaymentResponse(Esito esito){
        DetailsPaymentResponse detailsPaymentResponse = new DetailsPaymentResponse();
        detailsPaymentResponse.setStatus(esito);
        return detailsPaymentResponse;

    }

    public static RefundPaymentResponse refundPaymentResponse(EsitoStorno esitoStorno){
        RefundPaymentResponse refundPaymentResponse = new RefundPaymentResponse();
        refundPaymentResponse.setTransactionResult(esitoStorno);
        return refundPaymentResponse;
    }

    public static RefundPaymentRequest refundPaymentRequest(String authNumber){
        RefundPaymentRequest refundPaymentRequest = new RefundPaymentRequest();
        refundPaymentRequest.setMerchantId("merchantId");
        refundPaymentRequest.setShopId("shopIdTmp_APP");
        refundPaymentRequest.setShopTransactionId("1");
        refundPaymentRequest.setCurrency("978");
        refundPaymentRequest.setPaymentID("1234");
        refundPaymentRequest.setAuthNumber(authNumber);

        return refundPaymentRequest;
    }


    public static DetailsPaymentRequest detailsPaymentRequest(){
        DetailsPaymentRequest detailsPaymentRequest = new DetailsPaymentRequest();
        detailsPaymentRequest.setPaymentID("1234");
        detailsPaymentRequest.setShopTransactionId("1");
        detailsPaymentRequest.setShopId("shopIdTmp_APP");

        return detailsPaymentRequest;
   }

   public static PostePayPatchRequest postePayPatchRequest(){
        PostePayPatchRequest postePayPatchRequest = new PostePayPatchRequest(21L, "authCode", "correlation-ID");
        return postePayPatchRequest;

   }








}



