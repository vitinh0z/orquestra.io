package io.orchestra.cloud.infra.persistence.repository.payment;

import io.orchestra.cloud.infra.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findById(UUID id);

    Optional<PaymentEntity> findByIdempotecyKey(String idempotecyKey);
    
}
