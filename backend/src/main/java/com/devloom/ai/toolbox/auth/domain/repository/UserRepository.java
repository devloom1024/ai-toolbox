package com.devloom.ai.toolbox.auth.domain.repository;

import com.devloom.ai.toolbox.auth.domain.entity.UserEntity;
import com.devloom.ai.toolbox.auth.domain.enums.UserStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByIdAndStatusNot(Long id, UserStatus status);

    boolean existsByNickname(String nickname);
}
