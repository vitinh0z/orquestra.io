package io.orchestra.cloud.infra.persistence;

import io.orchestra.core.domain.entity.User;
import io.orchestra.core.domain.repository.user.UserRepository;
import io.orchestra.cloud.infra.persistence.entity.UserEntity;
import io.orchestra.cloud.infra.persistence.mapper.UserMapper;
import io.orchestra.cloud.infra.persistence.repository.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceGateway implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {

        UserEntity entity = userMapper.toEntity(user);
        UserEntity saveUser = userJpaRepository.save(entity);

        return userMapper.toDomain(saveUser);
    }
}
