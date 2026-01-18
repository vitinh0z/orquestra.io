package io.orchestra.infra.gateway;

import io.orchestra.domain.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public interface PaymentGateway {

    Payment process(Payment payment);
}
