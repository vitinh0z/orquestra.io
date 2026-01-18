package io.orchestra.infra.persistence.repository.gateway;

import io.orchestra.infra.persistence.gateway.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GatewayJpaRepository extends JpaRepository<GatewayEntity, UUID> {

    Optional<GatewayEntity> findByTenantIdAndGatewayName(UUID tenantId, String gatewayName);

    List<GatewayEntity> findAllByTenantIdAndIsActiveTrueOrderByPriorityAsc(UUID tenantId);
}
