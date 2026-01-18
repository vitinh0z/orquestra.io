package io.orchestra.infra.persistence;

import io.orchestra.domain.entity.User;
import io.orchestra.domain.repository.user.UserRepository;
import io.orchestra.infra.persistence.entity.UserEntity;
import io.orchestra.infra.persistence.mapper.UserMapper;
import io.orchestra.infra.persistence.repository.user.UserJpaRepository;
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
