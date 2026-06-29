-- Seed: candidate information form (general_info)
INSERT INTO general_info_category (name, position) VALUES
    ('Especialidad y Experiencia', 0),
    ('Nivel de Inglés',            1);

-- Category 1: Especialidad y Experiencia
INSERT INTO general_info_question (category_id, question, answer_type, position) VALUES
    (1, '¿Cuál es tu especialidad principal como coach?', 'SINGLE', 0),
    (1, '¿Cuántos años de experiencia tenés como coach?', 'SINGLE', 1);

-- Q1 options: especialidad
INSERT INTO general_info_option (question_id, answer, position) VALUES
    (1, 'Gimnasia Artística',    0),
    (1, 'Gimnasia Rítmica',      1),
    (1, 'Trampolín',             2),
    (1, 'Acrobacia en Tela',     3),
    (1, 'Aeróbica',              4),
    (1, 'Tumbling',              5),
    (1, 'Otra',                  6);

-- Q2 options: experiencia
INSERT INTO general_info_option (question_id, answer, position) VALUES
    (2, 'Menos de 1 año', 0),
    (2, '1–3 años',       1),
    (2, '3–5 años',       2),
    (2, '5–10 años',      3),
    (2, 'Más de 10 años', 4);

-- Category 2: Nivel de Inglés
INSERT INTO general_info_question (category_id, question, answer_type, position) VALUES
    (2, '¿Cuál es tu nivel de inglés?', 'SINGLE', 0);

-- Q3 options: inglés
INSERT INTO general_info_option (question_id, answer, position) VALUES
    (3, 'Ninguno',     0),
    (3, 'Básico',      1),
    (3, 'Intermedio',  2),
    (3, 'Avanzado',    3),
    (3, 'Nativo',      4);

-- Seed: onboarding research wizard — CANDIDATE (5 steps)
INSERT INTO onboarding_question (type, step_order, question) VALUES
    ('ONBOARDING_CAN', 0, '¿Qué te motivó a convertirte en coach de gimnasia?'),
    ('ONBOARDING_CAN', 1, '¿Cuál es el principal desafío que enfrentás en tu día a día como coach?'),
    ('ONBOARDING_CAN', 2, '¿Con qué frecuencia te capacitás en nuevas metodologías de entrenamiento?'),
    ('ONBOARDING_CAN', 3, '¿Qué tipo de institución describe mejor tu lugar de trabajo actual?'),
    ('ONBOARDING_CAN', 4, '¿Cuál es tu objetivo principal al usar esta plataforma?');

-- Q1-CAN options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (1, 'Fui atleta y quiero devolver lo que aprendí',       0),
    (1, 'Siempre me apasionó enseñar deportes',              1),
    (1, 'Encontré una oportunidad laboral en el sector',     2),
    (1, 'Influencia de un coach que tuve de niño/a',         3);

-- Q2-CAN options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (2, 'Falta de recursos y materiales',       0),
    (2, 'Motivar a los atletas de forma continua', 1),
    (2, 'Coordinar horarios y equipos',         2),
    (2, 'Comunicación con padres o clubes',     3);

-- Q3-CAN options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (3, 'Nunca o casi nunca',            0),
    (3, 'Una o dos veces al año',        1),
    (3, 'Cada pocos meses',              2),
    (3, 'Continuamente (online o presencial)', 3);

-- Q4-CAN options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (4, 'Club deportivo privado', 0),
    (4, 'Escuela pública',        1),
    (4, 'Academia independiente', 2),
    (4, 'Trabajo freelance',      3),
    (4, 'Federación nacional',    4);

-- Q5-CAN options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (5, 'Mostrar mis habilidades y obtener visibilidad', 0),
    (5, 'Conseguir un puesto mejor remunerado',          1),
    (5, 'Validar mi conocimiento técnico',               2),
    (5, 'Conectar con clubes o academias',               3);

-- Seed: onboarding research wizard — RECRUITER (5 steps)
INSERT INTO onboarding_question (type, step_order, question) VALUES
    ('ONBOARDING_REC', 0, '¿Qué tipo de organización representás?'),
    ('ONBOARDING_REC', 1, '¿Cuántos coaches buscás incorporar anualmente?'),
    ('ONBOARDING_REC', 2, '¿Cuál es la mayor dificultad al contratar coaches?'),
    ('ONBOARDING_REC', 3, '¿Qué nivel de experiencia requerís principalmente?'),
    ('ONBOARDING_REC', 4, '¿Cuál es tu objetivo principal al usar esta plataforma?');

-- Q1-REC options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (6, 'Club deportivo privado', 0),
    (6, 'Federación nacional',    1),
    (6, 'Academia de gimnasia',   2),
    (6, 'Escuela pública',        3),
    (6, 'Empresa de eventos deportivos', 4);

-- Q2-REC options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (7, '1 a 3 coaches',   0),
    (7, '4 a 10 coaches',  1),
    (7, '10 a 20 coaches', 2),
    (7, 'Más de 20',       3);

-- Q3-REC options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (8, 'No hay perfiles verificados disponibles',    0),
    (8, 'El proceso de selección es muy lento',       1),
    (8, 'Falta de información sobre el candidato',    2),
    (8, 'Alto costo de los procesos de selección',    3);

-- Q4-REC options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (9, 'Sin experiencia (primer empleo)', 0),
    (9, '1–3 años',                        1),
    (9, '3–5 años',                        2),
    (9, 'Más de 5 años',                   3);

-- Q5-REC options
INSERT INTO onboarding_option (question_id, text, position) VALUES
    (10, 'Encontrar coaches con habilidades verificadas',  0),
    (10, 'Agilizar el proceso de selección',               1),
    (10, 'Acceder a un pool de candidatos especializado',  2),
    (10, 'Reducir costos de reclutamiento',                3);