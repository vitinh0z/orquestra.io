package io.orchestra.infra.gateway;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import io.orchestra.domain.entity.Payment;
import io.orchestra.domain.entity.PaymentStatus;
import io.orchestra.infra.exception.GatewayNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("MERCADOPAGO")
@Slf4j
@RequiredArgsConstructor
public class MercadoPagoGateway implements PaymentGateway {

    @Override
    public Payment process(Payment payment, String apiKey) {

        MPRequestOptions requestOptions = MPRequestOptions.builder()
                .accessToken(apiKey).build();

        PaymentCreateRequest request =
                PaymentCreateRequest.builder().transactionAmount(payment.getMoney())
                        .description("Teste Orquestra.io")
                        .paymentMethodId("pix")
                        .payer(PaymentPayerRequest.builder()
                                .email(payment.getCustomerEmail())
                                .build())
                        .build();

        try {
            PaymentClient paymentClient = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = paymentClient.create(request, requestOptions);

            payment.setStatus(PaymentStatus.PENDING);
            payment.setGatewayTransactionId(String.valueOf(mpPayment.getId()));

            if (mpPayment.getPointOfInteraction() != null && mpPayment.getPointOfInteraction().getTransactionData() != null){

                String getQrCode = mpPayment.getPointOfInteraction().getTransactionData().getQrCode();
                String getQrcodeBase64 = mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64();

                payment.setQrCode(getQrCode);
                payment.setQrCodeBase64(getQrcodeBase64);
            }


        } catch (MPException | MPApiException e) {
            log.error("Erro no Mercado Pago [ID: {}]: {}", payment.getId(), e.getMessage());
            payment.setStatus(PaymentStatus.ERROR);
            throw new GatewayNotFoundException("Falha no pagamento");
        }

        return payment;
    }
}
