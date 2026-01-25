package io.orchestra.infra.gateway;

import io.orchestra.domain.entity.Payment;

public interface PaymentGateway {

    Payment process(Payment payment, String apiKey);
}
