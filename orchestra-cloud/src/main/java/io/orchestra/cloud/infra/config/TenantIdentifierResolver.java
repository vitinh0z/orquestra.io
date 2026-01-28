package io.orchestra.cloud.infra.config;

import io.orchestra.cloud.infra.persistence.entity.TenantEntity;
import io.orchestra.cloud.infra.tenant.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String>{

    private static final String DEFAULT_TENANT = "default_tenant";

    @Override
    public String resolveCurrentTenantIdentifier() {
        TenantEntity currentTenant = TenantContext.get();

        if (currentTenant != null && currentTenant.getTenantId() != null){
            return currentTenant.getTenantId().toString();
        }
        return DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
