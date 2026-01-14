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

    private String name;

    @Column(unique = true)
    private String apiKey;

    private boolean isActive;

}
