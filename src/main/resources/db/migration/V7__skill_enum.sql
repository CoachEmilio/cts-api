-- Replace free-text skill column with controlled enum values.
-- Map any existing dev data to uppercase enum names; unknown values are left
-- as-is and will fail the CHECK constraint (intentional: forces cleanup).
UPDATE skill_test SET skill = UPPER(TRIM(skill)) WHERE skill IS NOT NULL;

ALTER TABLE skill_test
    ADD CONSTRAINT chk_skill_enum
    CHECK (skill IN ('ACROBACIA','ARTISTICA','RITMICA','TRAMPOLIN','TUMBLING','AEROBICA','TELA'));