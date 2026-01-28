package io.orchestra.cloud.infra.persistence.repository.user;

import io.orchestra.cloud.infra.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserJpaRepository extends JpaRepository<UserEntity, Long>{

}
