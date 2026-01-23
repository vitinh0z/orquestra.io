package io.orchestra.infra.gateway;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import io.orchestra.domain.entity.Payment;
import io.orchestra.domain.entity.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component("STRIPE")
public class StripeGateway implements PaymentGateway {


    @Override
    public Payment process(Payment payment, String apiKey) {

        RequestOptions requestOptions = RequestOptions.builder()
                .setApiKey(apiKey).build();

        PaymentIntentCreateParams params = PaymentIntentCreateParams
                .builder().setAmount(payment.getMoney().multiply(new BigDecimal("100")).longValue())
                .setCurrency(payment.getCurrency().toLowerCase())
                .setPaymentMethod("pm_card_visa")
                .setConfirm(true).build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params, requestOptions);

            payment.setStatus(PaymentStatus.APPROVED);
            payment.setGatewayTransactionId(paymentIntent.getId());

        } catch (StripeException e){
            log.error("ERRO: {}", e.getMessage());
            payment.setStatus(PaymentStatus.ERROR);


        }

        return payment;
    }


}
