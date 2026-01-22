package io.orchestra.infra.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import io.orchestra.domain.entity.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "execution_history")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExecutionHistoryEntity {

    @org.hibernate.validator.constraints.UUID
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID tenantId;

    private String gatewayName;

    private String requestPayload;

    private String responsePayload;

    private PaymentStatus status;

    private double latencyMs;

    private LocalDateTime createdAt;

}
