-- Extend skill enum with coaching/management disciplines
ALTER TABLE skill_test DROP CONSTRAINT chk_skill_enum;

ALTER TABLE skill_test
    ADD CONSTRAINT chk_skill_enum
    CHECK (skill IN (
        'ACROBACIA','ARTISTICA','RITMICA','TRAMPOLIN','TUMBLING','AEROBICA','TELA',
        'METODOLOGIA','GESTION'
    ));
