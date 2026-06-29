-- Assessment: banco editable de tests, preguntas y opciones.
-- is_correct vive solo en esta tabla; nunca sale en DTOs de candidato.

CREATE TABLE skill_test (
    id         BIGSERIAL    PRIMARY KEY,
    skill      VARCHAR(100) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE question (
    id           BIGSERIAL    PRIMARY KEY,
    skill_test_id BIGINT      NOT NULL REFERENCES skill_test(id) ON DELETE CASCADE,
    text         TEXT         NOT NULL,
    position     INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE option (
    id          BIGSERIAL    PRIMARY KEY,
    question_id BIGINT       NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    text        TEXT         NOT NULL,
    is_correct  BOOLEAN      NOT NULL DEFAULT false,
    position    INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_question_skill_test ON question(skill_test_id);
CREATE INDEX idx_option_question     ON option(question_id);