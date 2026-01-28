package io.orchestra.cloud.infra.persistence.mapper;

import io.orchestra.core.domain.entity.User;
import io.orchestra.cloud.infra.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User model){
        if (model == null) return null;

        return new UserEntity(
                model.getId(),
                model.getTanentId(),
                model.getFirstName(),
                model.getLastName(),
                model.getEmail(),
                model.getPassword(),
                model.getDocument(),
                model.getBalance(),
                model.getUserType()
        );
    }

    public User toDomain (UserEntity userEntity){
        if (userEntity == null) return null;

        return new User(
                userEntity.getId(),
                userEntity.getTenantId(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getDocument(),
                userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.getBalance(),
                userEntity.getUserType()
        );
    }

}
