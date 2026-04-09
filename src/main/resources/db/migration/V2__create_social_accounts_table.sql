-- Tabla para almacenar cuentas sociales vinculadas a usuarios
CREATE TABLE IF NOT EXISTS social_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    profile_picture_url VARCHAR(500),
    linked_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_social_accounts_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    CONSTRAINT uk_provider_provider_id 
        UNIQUE (provider, provider_id)
);

-- Índice para búsquedas por usuario
CREATE INDEX idx_social_accounts_user_id ON social_accounts(user_id);

-- Índice para búsquedas por proveedor
CREATE INDEX idx_social_accounts_provider ON social_accounts(provider);
