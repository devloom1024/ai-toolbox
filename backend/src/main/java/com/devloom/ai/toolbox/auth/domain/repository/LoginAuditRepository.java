package com.devloom.ai.toolbox.auth.domain.repository;

import com.devloom.ai.toolbox.auth.domain.entity.LoginAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditRepository extends JpaRepository<LoginAuditEntity, Long> {
}
