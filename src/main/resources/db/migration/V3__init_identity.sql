-- app_user: nombre "app_user" porque "user" es palabra reservada en PostgreSQL.
CREATE TABLE app_user (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'CANDIDATE',
    plan       VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);