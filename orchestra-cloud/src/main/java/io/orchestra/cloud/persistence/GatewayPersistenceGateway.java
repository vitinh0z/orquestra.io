package io.orchestra.cloud.infra.persistence;

import io.orchestra.core.domain.entity.Gateway;
import io.orchestra.core.domain.repository.gateway.GatewayConfigurationRepository;
import io.orchestra.cloud.infra.persistence.gateway.GatewayEntity;
import io.orchestra.cloud.infra.persistence.mapper.GatewayConfigurationMapper;
import io.orchestra.cloud.infra.persistence.repository.gateway.GatewayJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GatewayPersistenceGateway implements GatewayConfigurationRepository {

    private final GatewayJpaRepository gatewayJpaRepository;
    private final GatewayConfigurationMapper gatewayConfigurationMapper;


    @Override
    public Gateway save(Gateway gateway) {

        GatewayEntity entity = gatewayConfigurationMapper.toEntity(gateway);
        GatewayEntity saveGateway = gatewayJpaRepository.save(entity);

        return gatewayConfigurationMapper.toDomain(saveGateway);
    }

    @Override
    public Optional<Gateway> findByTenantIdAndGatewayName(UUID tenantId, String gatewayName) {

        Optional<GatewayEntity> findGateway = gatewayJpaRepository
                .findByTenantIdAndGatewayName(tenantId, gatewayName);
        return findGateway.map(gatewayConfigurationMapper::toDomain);
    }
}
