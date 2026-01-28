package io.orchestra.cloud.infra.persistence.repository.tenant;

import io.orchestra.cloud.infra.persistence.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<TenantEntity, UUID> {

    Optional<TenantEntity> findByApiKey(String apiKey);
}
