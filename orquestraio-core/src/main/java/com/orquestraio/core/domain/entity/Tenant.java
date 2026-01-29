package com.orquestraio.core.domain.entity;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tenant {

    private UUID id;
    private String name;
    private String apiKey;
    private boolean isActive;

    public Tenant (String name){
        this.id = UUID.randomUUID();
        this.name = name;
        this.apiKey = "Oz_" + UUID.randomUUID().toString().replace("-", "");
        this.isActive = true;
    }


}
