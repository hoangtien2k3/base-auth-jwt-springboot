-- =============================================================================
-- MIGRATION: V3__Create_Refresh_Tokens_Table.sql
-- Description: Create refresh tokens table for JWT token rotation
-- =============================================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_is_revoked ON refresh_tokens(is_revoked);

-- Add comments
COMMENT ON TABLE refresh_tokens IS 'Stores refresh tokens for JWT token rotation strategy';
COMMENT ON COLUMN refresh_tokens.token IS 'Hashed refresh token value';
COMMENT ON COLUMN refresh_tokens.is_revoked IS 'Flag indicating if token has been revoked';
COMMENT ON COLUMN refresh_tokens.device_info IS 'Information about the device that requested the token';
COMMENT ON COLUMN refresh_tokens.ip_address IS 'IP address from which the token was requested';
