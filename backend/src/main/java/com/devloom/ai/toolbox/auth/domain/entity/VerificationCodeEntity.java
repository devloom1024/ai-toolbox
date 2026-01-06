package com.devloom.ai.toolbox.auth.domain.entity;

import com.devloom.ai.toolbox.auth.domain.enums.VerificationChannel;
import com.devloom.ai.toolbox.auth.domain.enums.VerificationScene;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_verification_code")
public class VerificationCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private VerificationChannel channel;

    @Enumerated(EnumType.STRING)
    private VerificationScene scene;

    private String identifier;

    private String code;

    @Column(name = "expire_at")
    private Instant expireAt;

    private boolean used;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
