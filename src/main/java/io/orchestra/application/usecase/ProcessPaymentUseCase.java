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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

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
    private final StringRedisTemplate redisTemplate;

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
            redisTemplate.delete(idempotencyKey);
            throw e;
        } finally {
            saveAudit(currentTenant, targetGatewayName, paymentRequest, resultPayment, resultPayment != null ? resultPayment.getStatus() : PaymentStatus.ERROR, errorMessage, startTime);
        }
    }

    private Optional<PaymentResponseDTO> checkIdempotency(PaymentRequestDTO payment) {
        Optional<Payment> existingPayment = paymentPersistenceGateway.findByIdempotecyKey(payment.idempotecyKey());
        if (existingPayment.isPresent()) {
            log.info("Chave de idempotência {} já processada e encontrada no banco de dados.", payment.idempotecyKey());
            return Optional.of(paymentMapper.toDto(existingPayment.get()));
        }

        String cachedResponse = redisTemplate.opsForValue().get(LOCK_PREFIX + payment.idempotecyKey());
        if (cachedResponse != null) {
            if ("LOCKED".equals(cachedResponse)) {
                throw new IllegalStateException("Pagamento já está em processamento.");
            }
            try {
                log.info("Resposta encontrada no cache para a chave de idempotência {}.", payment.idempotecyKey());
                return Optional.of(objectMapper.readValue(cachedResponse, PaymentResponseDTO.class));
            } catch (JacksonException e) {
                log.error("Erro ao desserializar resposta do cache: {}", e.getMessage());
            }
        }
        return Optional.empty();
    }

    private void acquirePaymentLock(String idempotencyKey) {
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "LOCKED", Duration.ofHours(24));
        if (!Boolean.TRUE.equals(lockAcquired)) {
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

        try {
            String jsonResponse = objectMapper.writeValueAsString(paymentDto);
            redisTemplate.opsForValue().set(idempotencyKey, jsonResponse, Duration.ofHours(24));
        } catch (Exception e) {
            log.error("Falha ao cachear a resposta do pagamento: {}", e.getMessage());
        }
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
