package io.orchestra.core.domain.repository.user;

import io.orchestra.core.domain.entity.User;

public interface UserRepository {

    User save(User user);
}
