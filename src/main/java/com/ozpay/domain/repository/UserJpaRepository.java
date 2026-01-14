package com.ozpay.domain.repository;

import com.ozpay.domain.entity.User;
import com.ozpay.infra.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserJpaRepository extends JpaRepository<UserEntity, Long>{

    UserEntity save(User user);
}
