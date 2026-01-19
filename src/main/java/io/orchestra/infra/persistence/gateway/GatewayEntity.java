package io.orchestra.infra.persistence.gateway;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Entity(name = "gateways")
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "gatewayName"}, name = "gateways")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GatewayEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String gatewayName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedCredential;

    // high number = high priority (1 = Principal, 2 = Backup)
    @Column(name = "priority", nullable = false)
    private Integer priority = 1;

    private boolean isActive = true;

}
