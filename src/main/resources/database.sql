CREATE TABLE IF NOT EXISTS users (
                                     id CHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL DEFAULT NULL,
    is_delete TINYINT(1) NOT NULL DEFAULT 0,
    is_activated TINYINT(1) NOT NULL DEFAULT 0,
    is_email_verified TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
    );

ALTER TABLE users
    ADD COLUMN is_admin TINYINT(1) DEFAULT 0;
