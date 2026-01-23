package io.orchestra.domain.service;

import io.orchestra.application.dto.PaymentRequestDTO;
import org.springframework.stereotype.Component;

@Component
public interface PaymentRouter {
    String chooseGateway(PaymentRequestDTO requestDTO);
}
