-- Link attempts to the user who started them
ALTER TABLE attempt ADD COLUMN user_id BIGINT NOT NULL REFERENCES app_user(id);

-- Candidate profile (1:1 with app_user)
CREATE TABLE candidate_profile (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL UNIQUE REFERENCES app_user(id),
    display_name VARCHAR(100),
    bio          TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);