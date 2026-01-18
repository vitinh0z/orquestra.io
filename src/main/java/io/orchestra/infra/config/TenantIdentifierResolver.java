package io.orchestra.infra.config;

import io.orchestra.infra.persistence.entity.TenantEntity;
import io.orchestra.infra.tenant.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String>{

    @Override
    public String resolveCurrentTenantIdentifier() {
        TenantEntity currentTenant = TenantContext.get();

        if (currentTenant != null && currentTenant.getTenantId() != null){
            return currentTenant.getTenantId().toString();
        }
        return "default_tenant";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
