package com.orquestraio.core.domain.repository.tenant;

import com.orquestraio.core.domain.entity.Tenant;

import java.util.Optional;

public interface TenantRepository {

    Tenant save(Tenant tenant);

    Optional<Tenant> findByApiKey(String apiKey);

}
