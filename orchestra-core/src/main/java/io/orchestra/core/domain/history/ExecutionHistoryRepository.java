package io.orchestra.core.domain.history;

import io.orchestra.core.domain.entity.PaymentStatus;

public interface ExecutionHistoryRepository {

    void saveLog(String tenantId,
                 String gatewayName,
                 String requestJson,
                 String responseJson,
                 PaymentStatus status,
                 long latencyMs
    );
}
