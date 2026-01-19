package io.orchestra.infra.gateway;

import io.orchestra.domain.entity.Payment;
import io.orchestra.domain.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("STRIPE")
public class StripeGateway implements PaymentGateway {

    @Override
    public Payment process(Payment payment) {

        if(payment.getMoney().doubleValue() == 99.99){
            payment.setStatus(PaymentStatus.ERROR);
            return payment;
        }

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setGatewayTransactionId("sk_" + UUID.randomUUID());

        return payment;
    }


}
