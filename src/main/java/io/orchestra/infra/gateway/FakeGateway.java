package io.orchestra.infra.gateway;

import io.orchestra.domain.entity.Payment;
import io.orchestra.domain.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("MOCK")
public class FakeGateway implements PaymentGateway{

    @Override
    public Payment process(Payment payment, String apiKey) {

        try {
            Thread.sleep(300); // Simulamos latência de 300ms
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(payment.getMoney().doubleValue() == 99.99){
            payment.setStatus(PaymentStatus.ERROR);
            return payment;
        } // apenas para teste

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setGatewayTransactionId("sk_" + UUID.randomUUID());

        return payment;
    }
}
