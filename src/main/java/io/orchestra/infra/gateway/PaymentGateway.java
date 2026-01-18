package io.orchestra.infra.gateway;

import io.orchestra.domain.entity.Payment;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public interface PaymentGateway {

    @Bean
    Payment process(Payment payment);
}
