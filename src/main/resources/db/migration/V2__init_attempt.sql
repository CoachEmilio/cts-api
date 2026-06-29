-- Attempt lifecycle: start → answer → submit → result
-- El server calcula el score; el cliente nunca manda un puntaje.

CREATE TABLE attempt (
    id            BIGSERIAL   PRIMARY KEY,
    skill_test_id BIGINT      NOT NULL REFERENCES skill_test(id),
    status        VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    submitted_at  TIMESTAMPTZ
);

CREATE TABLE answer (
    id          BIGSERIAL   PRIMARY KEY,
    attempt_id  BIGINT      NOT NULL REFERENCES attempt(id) ON DELETE CASCADE,
    question_id BIGINT      NOT NULL REFERENCES question(id),
    option_id   BIGINT      NOT NULL REFERENCES option(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_answer_attempt_question UNIQUE (attempt_id, question_id)
);

CREATE TABLE result (
    id            BIGSERIAL    PRIMARY KEY,
    attempt_id    BIGINT       NOT NULL UNIQUE REFERENCES attempt(id) ON DELETE CASCADE,
    score_pct     NUMERIC(5,2) NOT NULL,
    correct_count INT          NOT NULL,
    total_count   INT          NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_attempt_skill_test ON attempt(skill_test_id);
CREATE INDEX idx_answer_attempt     ON answer(attempt_id);
