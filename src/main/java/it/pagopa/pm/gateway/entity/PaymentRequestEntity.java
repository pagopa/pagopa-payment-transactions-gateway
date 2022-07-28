package it.pagopa.pm.gateway.entity;


import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "PP_PGS_REQUEST_INFO")
public class PaymentRequestEntity {

    @Id
    @Column(name = "ID", nullable = false)
    @SequenceGenerator(name = "SEQ_PGS_REQUEST_INFO", sequenceName = "SEQ_PGS_REQUEST_INFO", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PGS_REQUEST_INFO")
    private Long id;

    @Column(name = "GUID", nullable = false)
    private String guid;

    @Column(name = "RESOURCE_PATH")
    private String resourcePath;

    @Column(name = "CORRELATION_ID")
    private String correlationId;

    @Column(name = "AUTH_URL", nullable = false)
    private String authorizationUrl;

    @Column(name = "ENDPOINT", nullable = false)
    private String requestEndpoint;

    @Column(name = "CLIENT_ID", nullable = false)
    private String clientId;

    @Column(name = "REQUEST")
    private String jsonRequest;

    @Column(name = "AUTH_OUTCOME")
    private Boolean authorizationOutcome;

    @Column(name = "ID_TRANSACTION", nullable = false)
    private String idTransaction;

    @Column(name = "IS_PROCESSED")
    private Boolean isProcessed = false;

    @Column(name = "AUTH_CODE")
    private String authorizationCode;

    @Column(name = "MDC_INFO")
    private String mdcInfo;

    @Column(name = "IS_REFUNDED")
    private Boolean isRefunded = false;

    @Column(name = "IS_ONBOARDING")
    private Boolean isOnboarding;



}
