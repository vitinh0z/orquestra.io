package com.ozpay.infra.persistence;

import com.ozpay.domain.entity.Tenant;
import com.ozpay.domain.repository.tenant.TenantRepository;
import com.ozpay.infra.persistence.entity.TenantEntity;
import com.ozpay.infra.persistence.mapper.TenantMapper;
import com.ozpay.infra.persistence.repository.tenant.TenantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class TenantPersistenceGateway implements TenantRepository {

    private final TenantJpaRepository tenantJpaRepository;
    private final TenantMapper tenantMapper;


    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity entity = tenantMapper.toEntity(tenant);

        TenantEntity saveTenent = tenantJpaRepository.save(entity);

        return tenantMapper.toDomain(saveTenent);
    }

    @Override
    public Optional<Tenant> findByApiKey(String apiKey) {
        Optional<TenantEntity> entity = tenantJpaRepository.findByApiKey(apiKey);

        return entity.map(tenantMapper::toDomain);
    }
}
