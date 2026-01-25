package io.orchestra.infra.persistence.mapper;

import io.orchestra.domain.entity.Gateway;
import io.orchestra.infra.persistence.gateway.GatewayEntity;
import io.orchestra.infra.security.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GatewayConfigurationMapper {

    private final ObjectMapper objectMapper;
    private final CryptoService cryptoService;

    public GatewayEntity toEntity (Gateway gateway){
        if (gateway == null) return null;

        try{
            String json = objectMapper.writeValueAsString(gateway.getCredential());
            String encrypted = cryptoService.encrypted(json);

            return new GatewayEntity(
                    gateway.getId(),
                    gateway.getTenentId(),
                    gateway.getGatewayName(),
                    encrypted,
                    gateway.getPriority(),
                    gateway.isActive()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Gateway toDomain (GatewayEntity gatewayEntity){

        if (gatewayEntity == null) return null;

        try {
            String json = cryptoService.descrypt(gatewayEntity.getEncryptedCredential());

            Map<String, String> credentials = objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, String>>() {}
            );

            return new Gateway(
                    gatewayEntity.getId(),
                    gatewayEntity.getTenantId(),
                    gatewayEntity.getGatewayName(),
                    credentials,
                    gatewayEntity.getPriority(),
                    gatewayEntity.isActive()
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
