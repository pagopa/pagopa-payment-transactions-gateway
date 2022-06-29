package it.pagopa.pm.gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ExceptionsEnum {

    GENERIC_ERROR("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_ALREADY_PROCESSED("Transaction already processed", HttpStatus.UNAUTHORIZED),
    RESTAPI_CD_CLIENT_ERROR("Exception during call to RestapiCD", HttpStatus.FAILED_DEPENDENCY),
    TRANSACTION_NOT_FOUND("Transaction not found", HttpStatus.NOT_FOUND),
    TIMEOUT("Timeout", HttpStatus.GATEWAY_TIMEOUT),
    MISSING_FIELDS("Missing mandatory fields", HttpStatus.BAD_REQUEST),
    REFUND_REQUEST_ALREADY_PROCESSED("Refund request already processed", HttpStatus.OK),
    POSTEPAY_SERVICE_EXCEPTION("Exception during call to PostePay service", HttpStatus.BAD_GATEWAY),
    TRANSACTION_REFUND_NOT_AUTHORIZED("Refund operation not authorized", HttpStatus.BAD_REQUEST),
    PAYMENT_REQUEST_NOT_FOUND("Payment request not found", HttpStatus.NOT_FOUND);


    @Getter
    private final String description;
    @Getter
    private final HttpStatus restApiCode;

    ExceptionsEnum(String description, HttpStatus restApiCode) {
        this.description = description;
        this.restApiCode = restApiCode;
    }

}
