-- =============================================================================
-- MIGRATION: V2__Create_Roles_And_Permissions_Tables.sql
-- Description: Create roles and permissions tables for authorization
-- =============================================================================

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role_name CHECK (name IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_SUPER_ADMIN'))
);

-- Create permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles junction table (Many-to-Many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create role_permissions junction table (Many-to-Many)
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX idx_permissions_resource_action ON permissions(resource, action);

-- Add comments
COMMENT ON TABLE roles IS 'Stores role definitions for role-based access control';
COMMENT ON TABLE permissions IS 'Stores permission definitions for fine-grained access control';
COMMENT ON TABLE user_roles IS 'Junction table linking users to their assigned roles';
COMMENT ON TABLE role_permissions IS 'Junction table linking roles to their permissions';

-- Insert default roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_USER', 'Standard user role with basic permissions'),
    ('ROLE_ADMIN', 'Administrator role with elevated permissions'),
    ('ROLE_MODERATOR', 'Moderator role with content management permissions'),
    ('ROLE_SUPER_ADMIN', 'Super administrator with full system access');

-- Insert default permissions
INSERT INTO permissions (name, description, resource, action) VALUES
    ('USER_READ', 'Read user information', 'USER', 'READ'),
    ('USER_WRITE', 'Create and update user information', 'USER', 'WRITE'),
    ('USER_DELETE', 'Delete user accounts', 'USER', 'DELETE'),
    ('ROLE_READ', 'Read role information', 'ROLE', 'READ'),
    ('ROLE_WRITE', 'Create and update roles', 'ROLE', 'WRITE'),
    ('ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE'),
    ('PERMISSION_READ', 'Read permission information', 'PERMISSION', 'READ'),
    ('PERMISSION_WRITE', 'Create and update permissions', 'PERMISSION', 'WRITE'),
    ('PERMISSION_DELETE', 'Delete permissions', 'PERMISSION', 'DELETE');

-- Assign permissions to roles
-- ROLE_USER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_USER' AND p.name IN ('USER_READ');

-- ROLE_MODERATOR permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_MODERATOR' AND p.name IN ('USER_READ', 'USER_WRITE');

-- ROLE_ADMIN permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' AND p.name IN ('USER_READ', 'USER_WRITE', 'USER_DELETE', 'ROLE_READ', 'ROLE_WRITE');

-- ROLE_SUPER_ADMIN permissions (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_SUPER_ADMIN';
