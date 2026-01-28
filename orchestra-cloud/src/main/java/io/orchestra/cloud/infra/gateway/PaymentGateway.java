package io.orchestra.cloud.infra.gateway;

import io.orchestra.core.domain.entity.Payment;

public interface PaymentGateway {

    Payment process(Payment payment, String apiKey);
}
