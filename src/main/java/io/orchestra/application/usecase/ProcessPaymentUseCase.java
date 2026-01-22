package io.orchestra.application.usecase;

import io.orchestra.application.dto.PaymentRequestDTO;
import io.orchestra.application.dto.PaymentResponseDTO;
import io.orchestra.domain.entity.Gateway;
import io.orchestra.domain.entity.Payment;
import io.orchestra.infra.gateway.GatewayRegistry;
import io.orchestra.infra.gateway.PaymentGateway;
import io.orchestra.infra.persistence.GatewayPersistenceGateway;
import io.orchestra.infra.persistence.PaymentPersistenceGateway;
import io.orchestra.infra.persistence.entity.TenantEntity;
import io.orchestra.infra.persistence.mapper.PaymentRequestDtoMapper;
import io.orchestra.infra.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentUseCase {

    private final GatewayPersistenceGateway gatewayPersistenceGateway;
    private final GatewayRegistry gatewayRegistry;
    private final PaymentPersistenceGateway paymentPersistenceGateway;
    private final PaymentRequestDtoMapper paymentMapper;


    public PaymentResponseDTO execute(PaymentRequestDTO payment){

        TenantEntity current = TenantContext.get();

        if(current == null){
            throw new IllegalStateException("Tenant context is missing");
        }

        Gateway gatewayConfig = gatewayPersistenceGateway
                .findByTenantIdAndGatewayName(current.getTenantId(), "STRIPE")
                        .orElseThrow(() -> new RuntimeException("Gatwway Stripe não configurado para o Tenant: " + current.getName()));

        log.info("Iniciando pagamento para Tenant [{}] usando Gateway [{}]", current.getName(), gatewayConfig);

        PaymentGateway gateway = gatewayRegistry.getGateway(gatewayConfig.getGatewayName());

        String apiKey = gatewayConfig.getCredential().get("secretKey");

        Payment toDomain = paymentMapper.toDomain(payment);

        Payment processPayment = gateway.process(toDomain, apiKey);
        Payment savePayment = paymentPersistenceGateway.save(processPayment);
        return paymentMapper.toDto(savePayment);

    }

}
