package io.orchestra.infra.persistence;

import io.orchestra.domain.entity.PaymentStatus;
import io.orchestra.domain.history.ExecutionHistoryRepository;
import io.orchestra.infra.persistence.entity.ExecutionHistoryEntity;
import io.orchestra.infra.persistence.repository.ExecutionHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
@Slf4j
public class ExecutationHistoryPersistenceGateway implements ExecutionHistoryRepository {

    private final ExecutionHistoryJpaRepository executionHistoryJpaRepository;

    @Override
    public void saveLog(String tenantId, String gatewayName, String requestJson,
                        String responseJson, PaymentStatus status, long latencyMs
    ) {
        try {
            ExecutionHistoryEntity entity = new ExecutionHistoryEntity();
            entity.setTenantId(UUID.fromString(tenantId));
            entity.setGatewayName(gatewayName);
            entity.setRequestPayload(requestJson);
            entity.setResponsePayload(responseJson);
            entity.setStatus(status);
            entity.setLatencyMs(latencyMs);

            executionHistoryJpaRepository.save(entity);
            log.info("Log de execução salvo com sucesso");
        } catch (Exception e) {
            log.error("Não foi possível salvar o log de execução: {}", e.getMessage());
        }
    }
}
