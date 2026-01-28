package io.orchestra.core.domain.repository.tenant;

import io.orchestra.core.domain.entity.Tenant;

import java.util.Optional;

public interface TenantRepository {

    Tenant save(Tenant tenant);

    Optional<Tenant> findByApiKey(String apiKey);

}
