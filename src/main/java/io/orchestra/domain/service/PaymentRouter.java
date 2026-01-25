package io.orchestra.domain.service;

import io.orchestra.application.dto.PaymentRequestDTO;

public interface PaymentRouter {
    String chooseGateway(PaymentRequestDTO requestDTO);
}
