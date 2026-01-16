package com.ozpay.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantEntity {

    @Id
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String apiKey;

    @Column(nullable = false)
    private boolean isActive;

}
