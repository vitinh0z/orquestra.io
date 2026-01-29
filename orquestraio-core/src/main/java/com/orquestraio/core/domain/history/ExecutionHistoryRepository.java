package com.orquestraio.core.domain.history;

import com.orquestraio.core.domain.entity.PaymentStatus;

public interface ExecutionHistoryRepository {

    void saveLog(String tenantId,
                 String gatewayName,
                 String requestJson,
                 String responseJson,
                 PaymentStatus status,
                 long latencyMs
    );
}
