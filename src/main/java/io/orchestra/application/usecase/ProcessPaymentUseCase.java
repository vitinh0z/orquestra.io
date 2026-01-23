package io.orchestra.application.usecase;

import io.orchestra.application.dto.PaymentRequestDTO;
import io.orchestra.application.dto.PaymentResponseDTO;
import io.orchestra.domain.constant.GatewayConstants;
import io.orchestra.domain.entity.Gateway;
import io.orchestra.domain.entity.Payment;
import io.orchestra.domain.entity.PaymentStatus;
import io.orchestra.domain.service.PaymentRouter;
import io.orchestra.infra.exception.GatewayNotFoundException;
import io.orchestra.infra.gateway.GatewayRegistry;
import io.orchestra.infra.gateway.PaymentGateway;
import io.orchestra.infra.persistence.ExecutationHistoryPersistenceGateway;
import io.orchestra.infra.persistence.GatewayPersistenceGateway;
import io.orchestra.infra.persistence.PaymentPersistenceGateway;
import io.orchestra.infra.persistence.entity.TenantEntity;
import io.orchestra.infra.persistence.mapper.PaymentRequestDtoMapper;
import io.orchestra.infra.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentUseCase {

    private final GatewayPersistenceGateway gatewayPersistenceGateway;
    private final GatewayRegistry gatewayRegistry;
    private final PaymentPersistenceGateway paymentPersistenceGateway;
    private final PaymentRequestDtoMapper paymentMapper;
    private final ObjectMapper objectMapper;
    private final ExecutationHistoryPersistenceGateway executationHistoryPersistenceGateway;
    private final PaymentRouter paymentRouter;


    public PaymentResponseDTO execute(PaymentRequestDTO payment) {

        long start = System.currentTimeMillis();
        TenantEntity current = TenantContext.get();
        if (current == null) throw new IllegalStateException("Tenant context is missing");

        String targetGatewayName = paymentRouter.chooseGateway(payment);

        Gateway gatewayConfig = gatewayPersistenceGateway
                .findByTenantIdAndGatewayName(current.getTenantId(), "STRIPE")
                .orElseThrow(() -> new GatewayNotFoundException("Gateway "
                        + GatewayConstants.STRIPE + " não configurado para o Tenant: "
                        + current.getName()
                )
                );

        log.info("Iniciando pagamento para Tenant [{}] usando Gateway [{}]", current.getName(), gatewayConfig.getGatewayName());

        PaymentGateway gateway = gatewayRegistry.getGateway(gatewayConfig.getGatewayName());
        String apiKey = gatewayConfig.getCredential().get(GatewayConstants.SECRET_KEY_PARAM);
        Payment paymentDomain = paymentMapper.toDomain(payment);

        Payment resultPayment = null;
        String erroMsg = null;
        PaymentStatus status = PaymentStatus.ERROR;

        try {
            resultPayment = gateway.process(paymentDomain, apiKey);
            status = resultPayment.getStatus();

            return paymentMapper.toDto(paymentPersistenceGateway.save(resultPayment));

        } catch (Exception e) {
            erroMsg = e.getMessage();
            throw e;

        } finally {
            saveAudit(current, targetGatewayName , payment, resultPayment, status, erroMsg, start);
        }
    }

    private void saveAudit(TenantEntity tenant, String gateway, PaymentRequestDTO req, Payment res, PaymentStatus status, String error, long start) {
        try {
            long latency = System.currentTimeMillis() - start;
            String reqJson = objectMapper.writeValueAsString(req);
            String resJson = (error != null) ? "ERROR: " + error : objectMapper.writeValueAsString(res);

            executationHistoryPersistenceGateway.saveLog(
                    tenant.getTenantId().toString(), gateway, reqJson, resJson, status, latency
            );

        } catch (Exception e) {
            log.warn("Falha na auditoria: {}", e.getMessage());
        }
    }
}
