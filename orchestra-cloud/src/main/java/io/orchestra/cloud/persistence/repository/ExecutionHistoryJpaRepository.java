package io.orchestra.cloud.infra.persistence.repository;

import io.orchestra.cloud.infra.persistence.entity.ExecutionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExecutionHistoryJpaRepository extends JpaRepository<ExecutionHistoryEntity, UUID> {

    ExecutionHistoryEntity save(ExecutionHistoryEntity entity);

    Optional<ExecutionHistoryEntity> findById(UUID id);
}
