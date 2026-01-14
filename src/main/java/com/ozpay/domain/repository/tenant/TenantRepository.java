package com.ozpay.domain.repository.tenant;

import com.ozpay.domain.entity.Tenant;

import java.util.Optional;

public interface TenantRepository {

    Tenant save(Tenant tenant);

    Optional<Tenant> findByApiKey(String apiKey);

}
