package com.orquestraio.core.domain.repository.gateway;

import com.orquestraio.core.domain.entity.Gateway;

import java.util.Optional;
import java.util.UUID;

public interface GatewayConfigurationRepository {

    Gateway save (Gateway gateway);
    Optional<Gateway> findByTenantIdAndGatewayName(UUID tenantId, String gatewayName);
}
