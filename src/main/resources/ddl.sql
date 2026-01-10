-------------------------------------------
--- USERS
-------------------------------------------
CREATE TABLE IF NOT EXISTS users
(
    id                    BIGSERIAL PRIMARY KEY,
    username              VARCHAR(50)  NOT NULL UNIQUE,
    email                 VARCHAR(100) NOT NULL UNIQUE,
    password              VARCHAR(255) NOT NULL,
    first_name            VARCHAR(50),
    last_name             VARCHAR(50),
    phone_number          VARCHAR(20),
    is_email_verified     BOOLEAN               DEFAULT FALSE,
    is_account_locked     BOOLEAN               DEFAULT FALSE,
    failed_login_attempts INTEGER               DEFAULT 0,
    last_failed_login_at  TIMESTAMP,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at         TIMESTAMP,
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_email_verified ON users (is_email_verified);
CREATE INDEX idx_users_created_at ON users (created_at);

-------------------------------------------
--- ROLE
-------------------------------------------
CREATE TABLE IF NOT EXISTS roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role_name CHECK (name IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_SUPER_ADMIN'))
);

CREATE TABLE IF NOT EXISTS permissions
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource    VARCHAR(50)  NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id     BIGINT    NOT NULL,
    role_id     BIGINT    NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS role_permissions
(
    role_id       BIGINT    NOT NULL,
    permission_id BIGINT    NOT NULL,
    assigned_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions (role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions (permission_id);
CREATE INDEX idx_permissions_resource_action ON permissions (resource, action);

INSERT INTO roles (name, description)
VALUES ('ROLE_USER', 'Standard user role with basic permissions'),
       ('ROLE_ADMIN', 'Administrator role with elevated permissions'),
       ('ROLE_MODERATOR', 'Moderator role with content management permissions'),
       ('ROLE_SUPER_ADMIN', 'Super administrator with full system access');

INSERT INTO permissions (name, description, resource, action)
VALUES ('USER_READ', 'Read user information', 'USER', 'READ'),
       ('USER_WRITE', 'Create and update user information', 'USER', 'WRITE'),
       ('USER_DELETE', 'Delete user accounts', 'USER', 'DELETE'),
       ('ROLE_READ', 'Read role information', 'ROLE', 'READ'),
       ('ROLE_WRITE', 'Create and update roles', 'ROLE', 'WRITE'),
       ('ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE'),
       ('PERMISSION_READ', 'Read permission information', 'PERMISSION', 'READ'),
       ('PERMISSION_WRITE', 'Create and update permissions', 'PERMISSION', 'WRITE'),
       ('PERMISSION_DELETE', 'Delete permissions', 'PERMISSION', 'DELETE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_USER' AND p.name IN ('USER_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MODERATOR' AND p.name IN ('USER_READ', 'USER_WRITE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' AND p.name IN ('USER_READ', 'USER_WRITE', 'USER_DELETE', 'ROLE_READ', 'ROLE_WRITE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_SUPER_ADMIN';

-----------------------------------------------
--- REFRESH TOKENS
-----------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(500) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    is_revoked  BOOLEAN               DEFAULT FALSE,
    revoked_at  TIMESTAMP,
    device_info VARCHAR(255),
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_is_revoked ON refresh_tokens (is_revoked);
