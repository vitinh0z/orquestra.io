package io.orchestra.cloud.infra.service;

import io.orchestra.core.application.dto.PaymentRequestDTO;
import io.orchestra.core.domain.service.PaymentRouter;
import org.springframework.stereotype.Component;

@Component
public class MetadataPaymentRouter implements PaymentRouter {
    @Override
    public String chooseGateway(PaymentRequestDTO requestDTO) {
        if (requestDTO.metadata() != null && requestDTO.metadata().containsKey("gateway")){
            return requestDTO.metadata().get("gateway").toUpperCase();
        }
        return "STRIPE";
    }
}
