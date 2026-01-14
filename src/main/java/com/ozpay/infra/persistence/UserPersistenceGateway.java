package com.ozpay.infra.persistence;

import com.ozpay.domain.entity.User;
import com.ozpay.domain.repository.UserJpaRepository;
import com.ozpay.domain.repository.UserRepository;
import com.ozpay.infra.persistence.entity.UserEntity;
import com.ozpay.infra.persistence.mapper.UserMapper;
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
