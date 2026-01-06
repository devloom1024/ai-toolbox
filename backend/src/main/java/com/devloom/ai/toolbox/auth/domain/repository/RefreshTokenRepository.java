package com.devloom.ai.toolbox.auth.domain.repository;

import com.devloom.ai.toolbox.auth.domain.entity.RefreshTokenEntity;
import com.devloom.ai.toolbox.auth.domain.entity.UserEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenAndExpiresAtAfter(String token, Instant now);

    void deleteByUser(UserEntity user);

    void deleteByUserAndDevice(UserEntity user, String device);
}
