ALTER TABLE skill_test
    ADD COLUMN duration_minutes INT NOT NULL DEFAULT 10;

ALTER TABLE attempt
    ADD COLUMN deadline       TIMESTAMPTZ,
    ADD COLUMN violations_count INT NOT NULL DEFAULT 0;
