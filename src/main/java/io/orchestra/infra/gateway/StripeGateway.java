package io.orchestra.infra.gateway;

import io.orchestra.domain.entity.Payment;
import org.springframework.stereotype.Component;

@Component("stripe_gateway")
public class StripeGateway implements PaymentGateway {
    @Override
    public Payment process(Payment payment) {
        throw new UnsupportedOperationException("Stripe gateway processing is not yet implemented.");
    }


}
