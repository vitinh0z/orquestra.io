package io.orchestra.infra.tenant;

import io.orchestra.infra.persistence.entity.TenantEntity;

import java.util.UUID;

public class TenantContext {

    private static final ThreadLocal<TenantEntity> currentTenant = new ThreadLocal<>();

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
