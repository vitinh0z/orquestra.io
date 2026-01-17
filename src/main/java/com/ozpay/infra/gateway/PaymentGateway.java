package com.ozpay.infra.gateway;

import com.ozpay.domain.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public interface PaymentGateway {

    Payment process(Payment payment);
}
