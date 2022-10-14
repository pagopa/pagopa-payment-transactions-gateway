package it.pagopa.pm.gateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pm.gateway.client.restapicd.RestapiCdClientImpl;
import it.pagopa.pm.gateway.dto.*;
import it.pagopa.pm.gateway.dto.enums.TransactionStatusEnum;
import it.pagopa.pm.gateway.dto.xpay.*;
import it.pagopa.pm.gateway.entity.PaymentRequestEntity;
import it.pagopa.pm.gateway.exception.ExceptionsEnum;
import it.pagopa.pm.gateway.exception.RestApiException;
import it.pagopa.pm.gateway.repository.PaymentRequestRepository;
import it.pagopa.pm.gateway.service.XpayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static it.pagopa.pm.gateway.constant.ApiPaths.*;
import static it.pagopa.pm.gateway.constant.Headers.MDC_FIELDS;
import static it.pagopa.pm.gateway.constant.Headers.X_CLIENT_ID;
import static it.pagopa.pm.gateway.constant.Messages.*;
import static it.pagopa.pm.gateway.dto.enums.PaymentRequestStatusEnum.*;
import static it.pagopa.pm.gateway.utils.MdcUtils.setMdcFields;

@RestController
@Slf4j
@RequestMapping(REQUEST_PAYMENTS_XPAY)
public class XPayPaymentController {

    private static final String APP_ORIGIN = "APP";
    private static final String WEB_ORIGIN = "WEB";
    private static final List<String> VALID_CLIENT_ID = Arrays.asList(APP_ORIGIN, WEB_ORIGIN);
    private static final String EUR_CURRENCY = "978";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${xpay.response.urlredirect}")
    private String pgsResponseUrlRedirect;

    @Value("${xpay.request.responseUrl}")
    private String xpayResponseUrl;

    @Value("${xpay.apiKey}")
    private String apiKey;

    @Value("${xpay.secretKey}")
    private String secretKey;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private XpayService xpayService;

    @Autowired
    private RestapiCdClientImpl restapiCdClient;

    @PostMapping()
    public ResponseEntity<XPayAuthResponse> requestPaymentsXPay(@RequestHeader(value = X_CLIENT_ID) String clientId,
                                                                @RequestHeader(required = false, value = MDC_FIELDS) String mdcFields,
                                                                @RequestBody XPayAuthRequest pgsRequest) {
        if (!VALID_CLIENT_ID.contains(clientId)) {
            log.info("START - POST " + REQUEST_PAYMENTS_XPAY);
            log.error(String.format("Client id %s is not valid", clientId));
            return createXpayAuthResponse(BAD_REQUEST_MSG_CLIENT_ID, HttpStatus.BAD_REQUEST, null);
        }

        if (ObjectUtils.anyNull(pgsRequest) || pgsRequest.getGrandTotal().equals(BigInteger.ZERO)) {
            log.info("START POST - " + REQUEST_PAYMENTS_XPAY);
            log.error(BAD_REQUEST_MSG);
            return createXpayAuthResponse(BAD_REQUEST_MSG, HttpStatus.BAD_REQUEST, null);
        }

        String idTransaction = pgsRequest.getIdTransaction();
        log.info(String.format("START - POST %s for idTransaction %s", REQUEST_PAYMENTS_XPAY, idTransaction));
        setMdcFields(mdcFields);

        if (Objects.nonNull(paymentRequestRepository.findByIdTransaction(idTransaction))) {
            log.warn("Transaction " + idTransaction + " has already been processed previously");
            return createXpayAuthResponse(TRANSACTION_ALREADY_PROCESSED_MSG, HttpStatus.UNAUTHORIZED, null);
        }
        return createAuthPaymentXpay(pgsRequest, clientId, mdcFields);
    }

    @GetMapping(XPAY_AUTH)
    public ResponseEntity<XPayAuthPollingResponse> retrieveXpayAuthorizationResponse(@PathVariable String requestId,
                                                                                     @RequestHeader(required = false, value = MDC_FIELDS) String mdcFields) {
        log.info("START - GET XPay authorization response for requestId: " + requestId);
        setMdcFields(mdcFields);
        PaymentRequestEntity entity = paymentRequestRepository.findByGuid(requestId);
        if (Objects.isNull(entity) || !StringUtils.equals(entity.getRequestEndpoint(), REQUEST_PAYMENTS_XPAY)) {
            log.error("No XPay request entity has been found for requestId: " + requestId);
            XPayPollingResponseError error = new XPayPollingResponseError(404L, REQUEST_ID_NOT_FOUND_MSG);
            return createXPayAuthPollingResponse(HttpStatus.NOT_FOUND, error, null);
        }
        return createXPayAuthPollingResponse(HttpStatus.OK, null, entity);
    }

    @PostMapping(XPAY_RESUME)
    public ResponseEntity<String> resumeXPayPayment(@PathVariable String requestId,
                                                    @RequestBody XPayResumeRequest pgsRequest) {

        log.info(String.format("START - POST %s for requestId %s", REQUEST_PAYMENTS_XPAY + XPAY_RESUME, requestId));

        if (Objects.isNull(pgsRequest.getEsito())) {
            log.error(BAD_REQUEST_MSG);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BAD_REQUEST_MSG);
        }

        PaymentRequestEntity entity = paymentRequestRepository.findByGuid(requestId);

        if (Objects.isNull(entity)) {
            log.error("No XPay entity has been found for requestId: " + requestId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(REQUEST_ID_NOT_FOUND_MSG);
        }

        if (pgsRequest.getEsito().equals(EsitoXpay.OK)) {
            try {
                executeXPayPaymentCall(pgsRequest, requestId, entity);
            } catch (Exception e) {
                String errorMessage = GENERIC_ERROR_PAYMENT_MSG + requestId;
                log.error(errorMessage, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
            }
        } else {
            entity.setStatus(DENIED.name());
            paymentRequestRepository.save(entity);
        }

        String urlRedirect = StringUtils.join(pgsResponseUrlRedirect, requestId);
        log.info(String.format("END - POST %s for requestId %s", REQUEST_PAYMENTS_XPAY + XPAY_RESUME, requestId));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(urlRedirect)).build();
    }

    private ResponseEntity<XPayAuthResponse> createXpayAuthResponse(String errorMessage, HttpStatus status, String requestId) {
        XPayAuthResponse response = new XPayAuthResponse();
        if (Objects.nonNull(requestId)) {
            PaymentRequestEntity entity = paymentRequestRepository.findByGuid(requestId);
            response.setRequestId(requestId);
            response.setStatus(entity.getStatus());
        }
        if (StringUtils.isEmpty(errorMessage)) {
            String urlRedirect = StringUtils.join(pgsResponseUrlRedirect, requestId);
            response.setUrlRedirect(urlRedirect);
        } else {
            response.setError(errorMessage);
        }

        log.info(String.format("END - POST %s for requestId %s", REQUEST_PAYMENTS_XPAY, requestId));
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<XPayAuthResponse> createAuthPaymentXpay(XPayAuthRequest pgsRequest, String clientId, String mdcFields) {
        String transactionId = pgsRequest.getIdTransaction();
        log.info("START - requesting XPay payment authorization for transactionId " + transactionId);
        PaymentRequestEntity paymentRequestEntity = new PaymentRequestEntity();
        AuthPaymentXPayRequest xPayAuthRequest = createXpayAuthRequest(pgsRequest);
        generateRequestEntity(clientId, mdcFields, transactionId, paymentRequestEntity, xPayAuthRequest);
        xPayAuthRequest.setUrlRisposta(String.format(xpayResponseUrl, paymentRequestEntity.getGuid()));
        return executeXPayAuthorizationCall(xPayAuthRequest, paymentRequestEntity, transactionId);
    }

    @Async
    private ResponseEntity<XPayAuthResponse> executeXPayAuthorizationCall(AuthPaymentXPayRequest xPayRequest, PaymentRequestEntity requestEntity, String transactionId) {
        log.info("START - execute XPay payment authorization call for transactionId: " + transactionId);
        try {
            AuthPaymentXPayResponse response = xpayService.callAutenticazione3DS(xPayRequest);
            if (ObjectUtils.isEmpty(response)) {
                String errorMsg = "Response from XPay to /autenticazione3DS is empty";
                log.error(errorMsg);
                return createXpayAuthResponse(errorMsg, HttpStatus.OK, null);
            } else {
                requestEntity.setTimeStamp(String.valueOf(response.getTimeStamp()));
                XpayError xpayError = response.getErrore();
                if (ObjectUtils.isEmpty(xpayError)) {
                    requestEntity.setXpayHtml(response.getHtml());
                    requestEntity.setAuthorizationOutcome(true);
                } else {
                    requestEntity.setErrorCode(String.valueOf(xpayError.getCodice()));
                    requestEntity.setErrorMessage(xpayError.getMessaggio());
                    requestEntity.setStatus(DENIED.name());
                }
                paymentRequestRepository.save(requestEntity);
                log.info("END - XPay Request Payment Authorization for idTransaction " + transactionId);
                return createXpayAuthResponse(null, HttpStatus.OK, requestEntity.getGuid());
            }
        } catch (Exception e) {
            log.error(GENERIC_ERROR_MSG + transactionId + " cause: " + e.getCause() + " - " + e.getMessage(), e);
            return createXpayAuthResponse(GENERIC_ERROR_MSG + transactionId, HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    private ResponseEntity<XPayAuthPollingResponse> createXPayAuthPollingResponse(HttpStatus httpStatus, XPayPollingResponseError error, PaymentRequestEntity entity) {
        XPayAuthPollingResponse response = new XPayAuthPollingResponse();

        if (Objects.nonNull(error)) {
            log.info("START - create XPay polling response - error case");
            response.setError(error);
            return ResponseEntity.status(httpStatus).body(response);
        }

        String requestId = entity.getGuid();
        log.info("START - create XPay polling response for requestId: " + requestId);
        response.setStatus(entity.getStatus());
        response.setHtml(entity.getXpayHtml());

        if (ObjectUtils.allNotNull(entity.getErrorCode(), entity.getErrorMessage())) {
            response.setError(new XPayPollingResponseError(Long.valueOf(entity.getErrorCode()), entity.getErrorMessage()));
            return ResponseEntity.ok().body(response);
        }

        log.info("END - createXPayAuthPollingResponse for requestId " + requestId);
        return ResponseEntity.ok().body(response);
    }

    @Async
    private void executeXPayPaymentCall(XPayResumeRequest pgsRequest, String requestId, PaymentRequestEntity entity) throws Exception {
        try {
            saveRequestEntityFieldsForPayment(entity, pgsRequest);
            PaymentXPayRequest xpayRequest = createXPayPaymentRequest(entity, pgsRequest);

            PaymentXPayResponse response = xpayService.callPaga3DS(xpayRequest);
            if (response.getEsito().equals(EsitoXpay.OK)) {
                entity.setStatus(AUTHORIZED.name());
                entity.setAuthorizationCode(response.getCodiceAutorizzazione());
                entity.setAuthorizationOutcome(true);
            } else if (response.getEsito().equals(EsitoXpay.KO) || Objects.nonNull(response.getErrore())) {
                entity.setStatus(DENIED.name());
                entity.setAuthorizationOutcome(false);
            }
        } catch (Exception e) {
            log.error(GENERIC_ERROR_PAYMENT_MSG + requestId + " cause: " + e.getCause() + " - " + e.getMessage(), e);
            entity.setStatus(DENIED.name());
            throw e;
        } finally {
            Long transactionStatus = entity.getStatus().equals(AUTHORIZED.name()) ? TransactionStatusEnum.TX_AUTHORIZED_BY_PGS.getId() : TransactionStatusEnum.TX_REFUSED.getId();
            String authCode = entity.getAuthorizationCode();
            PatchRequest patchRequest = new PatchRequest(transactionStatus, authCode);
            String closePaymentResult = restapiCdClient.callPatchTransactionV2(Long.valueOf(entity.getIdTransaction()), patchRequest);
            log.info("Response from PATCH updateTransaction for requestId: " + requestId + " " + closePaymentResult);
            paymentRequestRepository.save(entity);
        }
    }

    private void saveRequestEntityFieldsForPayment(PaymentRequestEntity entity, XPayResumeRequest pgsRequest) {
        entity.setXpayNonce(pgsRequest.getXpayNonce());
        entity.setTimeStamp(pgsRequest.getXpayNonce());
        paymentRequestRepository.save(entity);
    }

    private PaymentXPayRequest createXPayPaymentRequest(PaymentRequestEntity entity, XPayResumeRequest pgsRequest) throws Exception {
        String idTransaction = entity.getIdTransaction();
        String codTrans = StringUtils.leftPad(idTransaction, 2, "0");

        String json = entity.getJsonRequest();
        AuthPaymentXPayRequest authRequest = OBJECT_MAPPER.readValue(json, AuthPaymentXPayRequest.class);

        BigInteger grandTotal = authRequest.getImporto();
        String mac = authRequest.getMac();

        if (!mac.equals(pgsRequest.getMac())) {
            log.error("Response mac not valid");
            throw new RestApiException(ExceptionsEnum.MAC_NOT_VALID);
        }

        PaymentXPayRequest request = new PaymentXPayRequest();
        request.setDivisa(Long.valueOf(EUR_CURRENCY));
        request.setApiKey(apiKey);
        request.setCodiceTransazione(codTrans);
        request.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        request.setMac(mac);
        request.setImporto(grandTotal);
        request.setXpayNonce(entity.getXpayNonce());

        return request;
    }

    private AuthPaymentXPayRequest createXpayAuthRequest(XPayAuthRequest pgsRequest) {
        String idTransaction = pgsRequest.getIdTransaction();
        String codTrans = StringUtils.leftPad(idTransaction, 2, "0");
        String timeStamp = String.valueOf(System.currentTimeMillis());
        BigInteger grandTotal = pgsRequest.getGrandTotal();
        String mac = createMac(codTrans, grandTotal, timeStamp);

        AuthPaymentXPayRequest xPayRequest = new AuthPaymentXPayRequest();
        xPayRequest.setApiKey(apiKey);
        xPayRequest.setImporto(grandTotal);
        xPayRequest.setCvv(pgsRequest.getCvv());
        xPayRequest.setPan(pgsRequest.getPan());
        xPayRequest.setDivisa(EUR_CURRENCY);
        xPayRequest.setMac(mac);
        xPayRequest.setScadenza(pgsRequest.getExpiryDate());
        xPayRequest.setTimeStamp(timeStamp);
        xPayRequest.setCodiceTransazione(codTrans);
        log.info("Request body to call autenticazione3DS created for transactionId " + idTransaction);
        return xPayRequest;
    }

    private void generateRequestEntity(String clientId, String mdcFields, String transactionId,
                                       PaymentRequestEntity paymentRequestEntity, AuthPaymentXPayRequest request) {
        paymentRequestEntity.setClientId(clientId);
        paymentRequestEntity.setGuid(UUID.randomUUID().toString());
        paymentRequestEntity.setRequestEndpoint(REQUEST_PAYMENTS_XPAY);
        paymentRequestEntity.setIdTransaction(transactionId);
        paymentRequestEntity.setMdcInfo(mdcFields);
        paymentRequestEntity.setTimeStamp(request.getTimeStamp());
        paymentRequestEntity.setStatus(CREATED.name());
        String jsonRequest = StringUtils.EMPTY;
        try {
            jsonRequest = OBJECT_MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.error("Error while serializing request as JSON. Request object is: " + request);
        }
        paymentRequestEntity.setJsonRequest(jsonRequest);
    }

    private String createMac(String codTrans, BigInteger importo, String timeStamp) {
        String macString = String.format("apiKey=%scodiceTransazione=%sdivisa=%simporto=%stimeStamp=%s%s",
                apiKey, codTrans, EUR_CURRENCY, importo, timeStamp, secretKey);
        return hashMac(macString);
    }

    private String hashMac(String macString) {
        String hash = StringUtils.EMPTY;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] in = digest.digest(macString.getBytes(StandardCharsets.UTF_8));

            final StringBuilder builder = new StringBuilder();
            for (byte b : in) {
                builder.append(String.format("%02x", b));
            }
            hash = builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("hashMac", e);
        }
        return hash;
    }

}
