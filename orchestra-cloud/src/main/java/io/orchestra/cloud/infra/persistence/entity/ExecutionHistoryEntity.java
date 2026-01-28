package io.orchestra.cloud.infra.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import io.orchestra.core.domain.entity.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "execution_history")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExecutionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "gateway_name", nullable = false)
    private String gatewayName;

    @Column(name = "request_payload", nullable = false)
    private String requestPayload;

    @Column(name = "response_payload", nullable = false)
    private String responsePayload;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "latency_ms", nullable = false)
    private long latencyMs;

    @Column(name = "created_at", nullable = false,
    columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

}
