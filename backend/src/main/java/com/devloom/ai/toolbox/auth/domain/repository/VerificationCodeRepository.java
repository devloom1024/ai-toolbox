package com.devloom.ai.toolbox.auth.domain.repository;

import com.devloom.ai.toolbox.auth.domain.entity.VerificationCodeEntity;
import com.devloom.ai.toolbox.auth.domain.enums.VerificationChannel;
import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {

    Optional<VerificationCodeEntity> findFirstByIdentifierAndChannelAndSceneAndUsedFalseOrderByCreatedAtDesc(
            String identifier, VerificationChannel channel, VerificationScene scene);

    long countByIdentifierAndChannelAndSceneAndCreatedAtAfter(
            String identifier, VerificationChannel channel, VerificationScene scene, Instant createdAfter);
}
