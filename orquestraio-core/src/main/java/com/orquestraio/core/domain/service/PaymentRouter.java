package com.orquestraio.core.domain.service;

import com.orquestraio.core.application.dto.PaymentRequestDTO;

public interface PaymentRouter {
    String chooseGateway(PaymentRequestDTO requestDTO);
}
