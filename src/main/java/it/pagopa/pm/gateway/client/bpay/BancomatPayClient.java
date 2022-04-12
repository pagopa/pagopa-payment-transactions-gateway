package it.pagopa.pm.gateway.client.bpay;

import it.pagopa.pm.gateway.client.bpay.generated.*;
import it.pagopa.pm.gateway.dto.*;
import it.pagopa.pm.gateway.utils.ClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.xml.bind.*;
import java.math.BigDecimal;

@Slf4j
public class BancomatPayClient {

    @Autowired
    private WebServiceTemplate webServiceTemplate;

    @Value("${bancomatPay.client.group.code}")
    public String GROUP_CODE;
    @Value("${bancomatPay.client.institute.code}")
    public String INSTITUTE_CODE;
    @Value("${bancomatPay.client.tag}")
    public String TAG;
    @Value("${bancomatPay.client.token}")
    public String TOKEN;

    private final ObjectFactory objectFactory = new ObjectFactory();

    public InserimentoRichiestaPagamentoPagoPaResponse sendPaymentRequest(BPayPaymentRequest request, String guid) {
        log.info("START sendPaymentRequest");
        InserimentoRichiestaPagamentoPagoPa inserimentoRichiestaPagamentoPagoPa = new InserimentoRichiestaPagamentoPagoPa();
        RequestInserimentoRichiestaPagamentoPagoPaVO requestInserimentoRichiestaPagamentoPagoPaVO = new RequestInserimentoRichiestaPagamentoPagoPaVO();
        requestInserimentoRichiestaPagamentoPagoPaVO.setContesto(createContesto(guid, request.getLanguage()));
        RichiestaPagamentoPagoPaVO richiestaPagamentoPagoPaVO = new RichiestaPagamentoPagoPaVO();
        richiestaPagamentoPagoPaVO.setIdPSP(request.getIdPsp()!=null? ClientUtils.INTESA_SP_CODICE_ABI :null);
        richiestaPagamentoPagoPaVO.setIdPagoPa(String.valueOf(request.getIdPagoPa()));
        richiestaPagamentoPagoPaVO.setImporto(BigDecimal.valueOf(request.getAmount()));
        richiestaPagamentoPagoPaVO.setNumeroTelefonicoCriptato(request.getEncryptedTelephoneNumber());
        richiestaPagamentoPagoPaVO.setCausale(request.getSubject());
        richiestaPagamentoPagoPaVO.setTag(TAG);
        requestInserimentoRichiestaPagamentoPagoPaVO.setRichiestaPagamentoPagoPa(richiestaPagamentoPagoPaVO);
        inserimentoRichiestaPagamentoPagoPa.setArg0(requestInserimentoRichiestaPagamentoPagoPaVO);
        log.info("Payment request to be sent to BPay: " + inserimentoRichiestaPagamentoPagoPa);
        JAXBElement<InserimentoRichiestaPagamentoPagoPa> objectFactoryInserimentoRichiestaPagamentoPagoPa = objectFactory.createInserimentoRichiestaPagamentoPagoPa(inserimentoRichiestaPagamentoPagoPa);
        JAXBElement<InserimentoRichiestaPagamentoPagoPaResponse> inserimentoRichiestaPagamentoPagoPaResponseJAXBElement;
        inserimentoRichiestaPagamentoPagoPaResponseJAXBElement = (JAXBElement<InserimentoRichiestaPagamentoPagoPaResponse>) webServiceTemplate.marshalSendAndReceive(objectFactoryInserimentoRichiestaPagamentoPagoPa);
        InserimentoRichiestaPagamentoPagoPaResponse inserimentoRichiestaPagamentoPagoPaResponse = inserimentoRichiestaPagamentoPagoPaResponseJAXBElement.getValue();
        log.info("END sendPaymentRequest");
        return inserimentoRichiestaPagamentoPagoPaResponse;
    }

    public StornoPagamentoResponse sendRefundRequest(BPayRefundRequest request, String guid) {
        log.info("START sendRefundRequest");
        RequestStornoPagamentoVO requestStornoPagamentoVO = new RequestStornoPagamentoVO();
        requestStornoPagamentoVO.setContesto(createContesto(guid, request.getLanguage()));
        requestStornoPagamentoVO.setIdPagoPa(String.valueOf(request.getIdPagoPa()));
        requestStornoPagamentoVO.setCausale(request.getSubject());
        StornoPagamento stornoPagamento = new StornoPagamento();
        stornoPagamento.setArg0(requestStornoPagamentoVO);
        log.info("Refund request to be sent to BPay: " + stornoPagamento);
        JAXBElement<StornoPagamento> stornoPagamentoJAXBElement = objectFactory.createStornoPagamento(stornoPagamento);
        JAXBElement<StornoPagamentoResponse> responseStornoPagamento = (JAXBElement<StornoPagamentoResponse>) webServiceTemplate.marshalSendAndReceive(stornoPagamentoJAXBElement);
        log.info("END sendRefundRequest");
        return responseStornoPagamento.getValue();
    }

    private ContestoVO createContesto(String guid, String language) {
        ContestoVO contestoVO = new ContestoVO();
        contestoVO.setGuid(guid);
        contestoVO.setToken(TOKEN);
        contestoVO.setLingua(LinguaEnum.fromValue(ClientUtils.getLanguageCode(language)));
        UtenteAttivoVO utenteVO = new UtenteAttivoVO();
        utenteVO.setCodUtente(null);
        utenteVO.setCodGruppo(GROUP_CODE);
        utenteVO.setCodIstituto(INSTITUTE_CODE);
        contestoVO.setUtenteAttivo(utenteVO);
        return contestoVO;
    }

}
