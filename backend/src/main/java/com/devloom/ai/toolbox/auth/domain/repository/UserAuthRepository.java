package com.devloom.ai.toolbox.auth.domain.repository;

import com.devloom.ai.toolbox.auth.domain.entity.UserAuthEntity;
import com.devloom.ai.toolbox.auth.domain.entity.UserEntity;
import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, Long> {

    Optional<UserAuthEntity> findByIdentityTypeAndIdentifier(IdentityType identityType, String identifier);

    boolean existsByIdentityTypeAndIdentifier(IdentityType identityType, String identifier);

    List<UserAuthEntity> findByUser(UserEntity user);
}
