package io.orchestra.cloud.infra.tenant;

import io.orchestra.cloud.infra.persistence.entity.TenantEntity;

public final class TenantContext {

    private static final ThreadLocal<TenantEntity> currentTenant = new ThreadLocal<>();

    private TenantContext(){
        throw new IllegalStateException("Utility class - use static methods only");
    }

    public static TenantEntity get(){
        return currentTenant.get();
    }

    public static void set(TenantEntity tenantEntity){
        currentTenant.set(tenantEntity);
    }

    public static void clear (){
        currentTenant.remove();
    }
}
