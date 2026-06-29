-- M7: extend app_user with onboarding fields
ALTER TABLE app_user
    ADD COLUMN full_name        VARCHAR(100),
    ADD COLUMN phone            VARCHAR(30),
    ADD COLUMN onboarding_complete BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN avatar_url       TEXT;

-- General information form structure (candidate professional profile)
CREATE TABLE general_info_category (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    position INT          NOT NULL
);

CREATE TABLE general_info_question (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES general_info_category (id),
    question    TEXT         NOT NULL,
    answer_type VARCHAR(20)  NOT NULL DEFAULT 'SINGLE',
    position    INT          NOT NULL
);

CREATE TABLE general_info_option (
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT      NOT NULL REFERENCES general_info_question (id),
    answer      TEXT        NOT NULL,
    position    INT         NOT NULL
);

-- Stores the candidate's selected option per question
CREATE TABLE user_general_info_answer (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES app_user (id),
    question_id BIGINT NOT NULL REFERENCES general_info_question (id),
    option_id   BIGINT NOT NULL REFERENCES general_info_option (id),
    UNIQUE (user_id, question_id)
);

-- Onboarding research wizard questions (5 per role type)
CREATE TABLE onboarding_question (
    id         BIGSERIAL   PRIMARY KEY,
    type       VARCHAR(20) NOT NULL CHECK (type IN ('ONBOARDING_CAN', 'ONBOARDING_REC')),
    step_order INT         NOT NULL,
    question   TEXT        NOT NULL
);

CREATE TABLE onboarding_option (
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT    NOT NULL REFERENCES onboarding_question (id),
    text        TEXT      NOT NULL,
    position    INT       NOT NULL
);

-- Stores the user's selected option per research question
CREATE TABLE user_research_answer (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES app_user (id),
    question_id BIGINT      NOT NULL REFERENCES onboarding_question (id),
    option_id   BIGINT      NOT NULL REFERENCES onboarding_option (id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, question_id)
);