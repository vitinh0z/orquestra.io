package io.orchestra.core.domain.service;

import io.orchestra.core.application.dto.PaymentRequestDTO;

public interface PaymentRouter {
    String chooseGateway(PaymentRequestDTO requestDTO);
}
