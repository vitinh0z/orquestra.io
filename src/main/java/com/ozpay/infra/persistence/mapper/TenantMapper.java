package com.ozpay.infra.persistence.mapper;

import com.ozpay.domain.entity.Tenant;
import com.ozpay.infra.persistence.entity.TenantEntity;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    public TenantEntity toEntity(Tenant tenant){
        if(tenant == null) return null;
        return new TenantEntity(
                tenant.getId(),
                tenant.getName(),
                tenant.getApiKey(),
                tenant.isActive()
        );
    }

    public Tenant toDomain(TenantEntity tenantEntity){
        if (tenantEntity == null) return null;
        return new Tenant(
                tenantEntity.getTenantId(),
                tenantEntity.getName(),
                tenantEntity.getApiKey(),
                tenantEntity.isActive()
        );
    }
}
