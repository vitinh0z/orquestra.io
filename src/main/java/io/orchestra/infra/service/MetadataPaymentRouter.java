package io.orchestra.infra.service;

import io.orchestra.application.dto.PaymentRequestDTO;
import io.orchestra.domain.service.PaymentRouter;
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
