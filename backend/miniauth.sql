-- 1. 사용자 테이블
CREATE TABLE users (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   email VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   created_at TIMESTAMP DEFAULT current_timestamp()
);

-- 2. Refresh Token 관리 테이블
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE, -- 토큰 문자열
    expiry_date DATETIME NOT NULL,      -- 만료 시간
    is_revoked BOOLEAN DEFAULT FALSE,   -- 토큰 무효화 여부 (탈취 의심 시 사용)
    created_at TIMESTAMP DEFAULT current_timestamp(),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);