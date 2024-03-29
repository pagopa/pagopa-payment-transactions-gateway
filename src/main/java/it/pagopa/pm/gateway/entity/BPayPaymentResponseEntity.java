package it.pagopa.pm.gateway.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "PP_BPAY_PAYMENT_RESPONSE")
public class BPayPaymentResponseEntity {

    @Id
    @Column(name = "ID", nullable = false)
    @SequenceGenerator(name = "SEQ_BPAY_PAYMENT_RESPONSE", sequenceName = "SEQ_BPAY_PAYMENT_RESPONSE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BPAY_PAYMENT_RESPONSE")
    private Long id;

    @Column(name = "ID_PAGOPA", nullable = false)
    private Long idPagoPa;

    @Column(name = "OUTCOME", nullable = false)
    private Boolean outcome;

    @Column(name = "ERROR_CODE", nullable = false)
    private String errorCode;

    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @Column(name = "CORRELATION_ID", nullable = false)
    private String correlationId;

    @Column(name = "CLIENT_GUID")
    private String clientGuid;

    @Column(name = "IS_PROCESSED")
    private Boolean isProcessed = false;

    @Column(name = "MDC_INFO")
    private String mdcInfo;

    @Column(name = "ABI")
    private String abi;

}
