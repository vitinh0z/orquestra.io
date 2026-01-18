package io.orchestra.domain.repository.user;

import io.orchestra.domain.entity.User;

public interface UserRepository {

    User save(User user);
}
