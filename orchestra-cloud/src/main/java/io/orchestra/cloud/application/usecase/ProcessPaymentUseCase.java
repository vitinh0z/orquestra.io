package io.orchestra.cloud.application.usecase;

import io.orchestra.core.application.dto.PaymentRequestDTO;
import io.orchestra.core.application.dto.PaymentResponseDTO;
import io.orchestra.cloud.infra.exception.GatewayNotFoundException;
import io.orchestra.cloud.infra.gateway.GatewayRegistry;
import io.orchestra.cloud.infra.gateway.PaymentGateway;
import io.orchestra.cloud.infra.persistence.ExecutationHistoryPersistenceGateway;
import io.orchestra.cloud.infra.persistence.GatewayPersistenceGateway;
import io.orchestra.cloud.infra.persistence.PaymentPersistenceGateway;
import io.orchestra.cloud.infra.persistence.entity.TenantEntity;
import io.orchestra.cloud.infra.persistence.mapper.PaymentRequestDtoMapper;
import io.orchestra.cloud.infra.tenant.TenantContext;
import io.orchestra.core.domain.constant.GatewayConstants;
import io.orchestra.core.domain.entity.Gateway;
import io.orchestra.core.domain.entity.Payment;
import io.orchestra.core.domain.entity.PaymentStatus;
import io.orchestra.core.domain.service.PaymentRouter;
import io.orchestra.core.service.IdempotencyService;
import io.orchestra.core.service.LockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

/**
 * Refactored ProcessPaymentUseCase using abstraction layers.
 * Now depends on IdempotencyService and LockProvider interfaces instead of concrete Redis implementation.
 * This allows the code to work with in-memory implementations (from core) or Redis (from cloud).
 */
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
    
    // Using abstractions instead of concrete Redis implementation
    private final IdempotencyService idempotencyService;
    private final LockProvider lockProvider;

    private static final String LOCK_PREFIX = "lock:payment:";

    public PaymentResponseDTO execute(PaymentRequestDTO paymentRequest) {
        Optional<PaymentResponseDTO> idempotentResponse = checkIdempotency(paymentRequest);
        if (idempotentResponse.isPresent()) {
            return idempotentResponse.get();
        }

        long startTime = System.currentTimeMillis();
        TenantEntity currentTenant = TenantContext.get();
        if (currentTenant == null) {
            throw new IllegalStateException("Tenant context is missing");
        }

        String idempotencyKey = LOCK_PREFIX + paymentRequest.idempotecyKey();
        Payment resultPayment = null;
        String errorMessage = null;
        String targetGatewayName = null;

        try {
            acquirePaymentLock(idempotencyKey);

            Gateway gatewayConfig = findAndConfigureGateway(paymentRequest);
            targetGatewayName = gatewayConfig.getGatewayName();
            log.info("Iniciando pagamento para Tenant [{}] usando Gateway [{}]", currentTenant.getName(), targetGatewayName);

            PaymentGateway gateway = gatewayRegistry.getGateway(targetGatewayName);
            String apiKey = gatewayConfig.getCredential().get(GatewayConstants.SECRET_KEY_PARAM);
            Payment paymentToProcess = paymentMapper.toDomain(paymentRequest);
            paymentToProcess.setStatus(PaymentStatus.PENDING);

            resultPayment = processPayment(gateway, apiKey, paymentToProcess);

            return handleSuccessfulPayment(resultPayment, idempotencyKey);

        } catch (Exception e) {
            log.error("Falha ao processar pagamento para a chave de idempotência {}: {}", idempotencyKey, e.getMessage());
            errorMessage = e.getMessage();
            lockProvider.unlock(idempotencyKey);
            throw e;
        } finally {
            saveAudit(currentTenant, targetGatewayName, paymentRequest, resultPayment, resultPayment != null ? resultPayment.getStatus() : PaymentStatus.ERROR, errorMessage, startTime);
        }
    }

    private Optional<PaymentResponseDTO> checkIdempotency(PaymentRequestDTO payment) {
        // First check database for processed payments
        Optional<Payment> existingPayment = paymentPersistenceGateway.findByIdempotecyKey(payment.idempotecyKey());
        if (existingPayment.isPresent()) {
            log.info("Chave de idempotência {} já processada e encontrada no banco de dados.", payment.idempotecyKey());
            return Optional.of(paymentMapper.toDto(existingPayment.get()));
        }

        // Then check cache using abstraction
        String idempotencyKey = LOCK_PREFIX + payment.idempotecyKey();
        Optional<PaymentResponseDTO> cachedResponse = idempotencyService.getCachedResponse(
            idempotencyKey, 
            PaymentResponseDTO.class
        );
        
        if (cachedResponse.isPresent()) {
            log.info("Resposta encontrada no cache para a chave de idempotência {}.", payment.idempotecyKey());
            return cachedResponse;
        }
        
        // Check if currently being processed (locked)
        if (lockProvider.isLocked(idempotencyKey)) {
            throw new IllegalStateException("Pagamento já está em processamento.");
        }
        
        return Optional.empty();
    }

    private void acquirePaymentLock(String idempotencyKey) {
        boolean lockAcquired = lockProvider.tryLock(idempotencyKey, "LOCKED", Duration.ofHours(24));
        if (!lockAcquired) {
            throw new IllegalStateException("Pagamento já está em processamento.");
        }
    }

    private Gateway findAndConfigureGateway(PaymentRequestDTO payment) {
        String targetGatewayName = paymentRouter.chooseGateway(payment);
        TenantEntity currentTenant = TenantContext.get();

        return gatewayPersistenceGateway
                .findByTenantIdAndGatewayName(currentTenant.getTenantId(), targetGatewayName)
                .orElseThrow(() -> new GatewayNotFoundException("Gateway "
                        + targetGatewayName + " não configurado para o Tenant: "
                        + currentTenant.getName()
                ));
    }

    private Payment processPayment(PaymentGateway gateway, String apiKey, Payment paymentDomain) {
        try {
            return gateway.process(paymentDomain, apiKey);
        } catch (Exception gatewayError) {
            log.error("Falha ao realizar pagamento no gateway: {}", gatewayError.getMessage());
            throw gatewayError;
        }
    }

    private PaymentResponseDTO handleSuccessfulPayment(Payment payment, String idempotencyKey) {
        Payment savedPayment = paymentPersistenceGateway.save(payment);
        PaymentResponseDTO paymentDto = paymentMapper.toDto(savedPayment);

        // Cache the response using abstraction
        idempotencyService.cacheResponse(idempotencyKey, paymentDto, Duration.ofHours(24));
        
        return paymentDto;
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
            log.warn("Falha ao salvar log de auditoria: {}", e.getMessage());
        }
    }
}
