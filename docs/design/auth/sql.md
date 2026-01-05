# 身份认证 SQL 设计

> 本文档基于 `docs/specs/mysql.md` 的规范，整理可直接用于 Flyway 的 DDL 模板，所有字段均采用 `NOT NULL` 并指定默认值。

## t_user
```sql
CREATE TABLE t_user (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '用户主键',
  nickname VARCHAR(64) NOT NULL DEFAULT '' COMMENT '昵称',
  avatar VARCHAR(512) NOT NULL DEFAULT '' COMMENT '头像 URL',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1=正常 2=锁定 3=注销',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户画像';
```

## t_user_auth
```sql
CREATE TABLE t_user_auth (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '记录主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '关联 t_user.id',
  identity_type VARCHAR(32) NOT NULL COMMENT '认证方式：EMAIL/PHONE/LINUX_DO 等',
  identifier VARCHAR(255) NOT NULL COMMENT '唯一标识（邮箱/手机号/第三方 ID）',
  credential VARCHAR(255) NOT NULL DEFAULT '' COMMENT '凭证：BCrypt 密码或第三方令牌摘要',
  verified TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=未验证 1=已验证',
  last_login_at TIMESTAMP NULL DEFAULT NULL COMMENT '最近登录时间',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_identity_type_identifier (identity_type, identifier),
  KEY idx_user_id (user_id),
  CONSTRAINT fk_user_auth_user FOREIGN KEY (user_id) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证方式';
```

## t_verification_code
```sql
CREATE TABLE t_verification_code (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '记录主键',
  channel VARCHAR(16) NOT NULL COMMENT '渠道：EMAIL/PHONE',
  identifier VARCHAR(255) NOT NULL COMMENT '邮箱或手机号',
  code VARCHAR(10) NOT NULL COMMENT '验证码',
  expire_at TIMESTAMP NOT NULL COMMENT '过期时间',
  used TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_identifier_channel (identifier, channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码审计';
```

## t_refresh_token
```sql
CREATE TABLE t_refresh_token (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '记录主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
  token CHAR(64) NOT NULL COMMENT 'Refresh Token 摘要',
  device VARCHAR(64) NOT NULL DEFAULT '' COMMENT '终端标识',
  expires_at TIMESTAMP NOT NULL COMMENT '过期时间',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_token (token),
  KEY idx_user_device (user_id, device)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Refresh Token 管理';
```

## t_login_audit
```sql
CREATE TABLE t_login_audit (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '记录主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
  identity_type VARCHAR(32) NOT NULL COMMENT '登录方式',
  identifier VARCHAR(255) NOT NULL COMMENT '登录标识（邮箱/手机号等）',
  ip VARCHAR(64) NOT NULL DEFAULT '' COMMENT '登录 IP',
  user_agent VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'UA',
  status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1=成功 2=失败',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  KEY idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录审计';
```

> 若需扩展第三方资料（如 LinuxDo 返回的头像、邮箱），可新增 `t_user_oauth_profile`，结构参照上述规范，字段命名与索引遵循相同约定。
