package com.devloom.ai.toolbox.auth.domain.repository;

import com.devloom.ai.toolbox.auth.domain.entity.UserOauthProfileEntity;
import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOauthProfileRepository extends JpaRepository<UserOauthProfileEntity, Long> {

    Optional<UserOauthProfileEntity> findByIdentityTypeAndOauthUserId(IdentityType identityType, String oauthUserId);

    boolean existsByIdentityTypeAndOauthUserId(IdentityType identityType, String oauthUserId);
}
