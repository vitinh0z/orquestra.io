package com.ozpay.infra.persistence.repository.tenant;

import com.ozpay.infra.persistence.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<TenantEntity, UUID> {

    Optional<TenantEntity> findByApiKey(String apiKey);
}
