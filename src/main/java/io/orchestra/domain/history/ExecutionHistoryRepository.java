package io.orchestra.domain.history;

import io.orchestra.domain.entity.PaymentStatus;

public interface ExecutionHistoryRepository {

    void saveLog(String tenantId,
                 String gatewayName,
                 String requestJson,
                 String responseJson,
                 PaymentStatus status,
                 long latencyMs
    );
}
