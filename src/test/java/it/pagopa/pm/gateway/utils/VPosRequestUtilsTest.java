package it.pagopa.pm.gateway.utils;

import it.pagopa.pm.gateway.beans.ValidBeans;
import it.pagopa.pm.gateway.dto.creditcard.StepZeroRequest;
import it.pagopa.pm.gateway.dto.vpos.MethodCompletedEnum;
import it.pagopa.pm.gateway.dto.vpos.Shop;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = VPosRequestUtils.class)
public class VPosRequestUtilsTest {

    @Mock
    VPosUtils vPosUtils;

    @Spy
    @InjectMocks
    private VPosRequestUtils vPosRequestUtils = new VPosRequestUtils("http://localhost:8080/payment-gateway/",
            "http://localhost:8080/payment-gateway/", vPosUtils);

    private static final String CORRELATION_ID = "correlationId";

    @Test
    public void generateRequestForStep0_FirstPayment_Test() throws Exception {
        Boolean isFisrtPayment = true;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildStepZeroRequestParams(pgsRequest, "requestId"));
    }

    @Test
    public void generateRequestForStep0_Not_FirstPayment_Test() throws Exception {
        Boolean isFisrtPayment = false;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildStepZeroRequestParams(pgsRequest, "requestId"));
    }

    @Test
    public void generateRequestForAccount_FirstPayment_Test() throws IOException {
        Boolean isFisrtPayment = true;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildAccountingRequestParams(pgsRequest, CORRELATION_ID));
    }

    @Test
    public void generateRequestForAccount_Not_FirstPayment_Test() throws IOException {
        Boolean isFisrtPayment = false;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildAccountingRequestParams(pgsRequest, CORRELATION_ID));
    }

    @Test
    public void generateRequestForRevert_FirstPayment_Test() throws IOException {
        Boolean isFisrtPayment = true;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildRevertRequestParams(pgsRequest, "correlationId"));
    }

    @Test
    public void generateRequestForRevert_Not_FirstPayment_Test() throws IOException {
        Boolean isFisrtPayment = false;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildRevertRequestParams(pgsRequest, CORRELATION_ID));
    }

    @Test
    public void generateRequestForStep1_First_Payment_Test() throws IOException {
        Boolean isFisrtPayment = true;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        MethodCompletedEnum methodCompletedEnum = MethodCompletedEnum.Y;
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildStepOneRequestParams(methodCompletedEnum, pgsRequest, "correlationId"));
    }

    @Test
    public void generateRequestForStep1_Not_FirstPayment_Test() throws IOException {
        Boolean isFisrtPayment = false;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        MethodCompletedEnum methodCompletedEnum = MethodCompletedEnum.Y;
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildStepOneRequestParams(methodCompletedEnum, pgsRequest, "correlationId"));
    }

    @Test
    public void generateRequestForStep2_First_Payment_Test() throws IOException {
        Boolean isFisrtPayment = true;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildStepTwoRequestParams(pgsRequest, "correlationId"));
    }

    @Test
    public void generateRequestForStep2_Not_FirstPayment_Test() throws IOException {
        Boolean isFisrtPayment = false;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildStepTwoRequestParams(pgsRequest, "correlationId"));
    }

    @Test
    public void generateRequestForOrderStatus_First_Payment_Test() throws IOException {
        Boolean isFisrtPayment = true;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildOrderStatusParams(pgsRequest));
    }

    @Test
    public void generateRequestForOrderStatus_Not_FirstPayment_Test() throws IOException {
        Boolean isFisrtPayment = false;
        StepZeroRequest pgsRequest = ValidBeans.createStep0Request(isFisrtPayment);
        Shop shop = ValidBeans.generateShop("321");
        when(vPosUtils.getReqRefNum()).thenReturn("reqRefNum");
        when(vPosUtils.getVposShopByIdPsp(any())).thenReturn(shop);
        assertNotNull(vPosRequestUtils.buildOrderStatusParams(pgsRequest));
    }

}
