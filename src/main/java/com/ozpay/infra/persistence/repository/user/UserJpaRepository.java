package com.ozpay.infra.persistence.repository.user;

import com.ozpay.infra.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserJpaRepository extends JpaRepository<UserEntity, Long>{

}
