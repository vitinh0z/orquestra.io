package io.orchestra.infra.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GatewayRegistry {


    private final Map<String, PaymentGateway> gatewayMap;

    public PaymentGateway getGeteway(String gatewayName){

        PaymentGateway paymentGateway = gatewayMap.get(gatewayName);

        if (paymentGateway == null){
            throw new RuntimeException("Gateway "+ gatewayName + "not found");
        }

        return paymentGateway;
    }
}
