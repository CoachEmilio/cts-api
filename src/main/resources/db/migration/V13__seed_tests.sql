-- Seed: 10 tests de gimnasia con 5 preguntas cada uno (50 preguntas en total)
-- Skills: ACROBACIA x1, ARTISTICA x3, RITMICA x1, METODOLOGIA x2, GESTION x3

-- ──────────────────────────────────────────────────────────
-- TEST 1: ACROBACIA — Fundamentos del Coaching en Acrobacia
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('ACROBACIA', 'Fundamentos del Coaching en Acrobacia', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuál es el principio de progresión correcto al enseñar un salto mortal hacia atrás agrupado?', 1),
    (currval('skill_test_id_seq'), '¿Qué acción del entrenador es fundamental para garantizar la seguridad durante el aprendizaje de habilidades aéreas?', 2),
    (currval('skill_test_id_seq'), '¿Cuál de los siguientes elementos NO forma parte de una sesión de calentamiento adecuada para acrobacia?', 3),
    (currval('skill_test_id_seq'), '¿Qué tipo de retroalimentación resulta más efectiva cuando un atleta aprende una nueva habilidad acrobática?', 4),
    (currval('skill_test_id_seq'), '¿Cuándo es apropiado retirar el apoyo manual (spotting) en una habilidad acrobática?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'Enseñar el salto mortal completo desde el inicio para que el atleta sienta la habilidad real', false, 1),
    (currval('question_id_seq') - 4, 'Construir desde habilidades previas: rondada, flic-flac y rotaciones hacia atrás en trampolín', true, 2),
    (currval('question_id_seq') - 4, 'Comenzar directamente con el elemento más difícil para motivar al atleta', false, 3),
    (currval('question_id_seq') - 4, 'Omitir los ejercicios preparatorios si el atleta tiene buena condición física', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'Dejar al atleta intentar la habilidad de forma autónoma para desarrollar confianza', false, 1),
    (currval('question_id_seq') - 3, 'Proporcionar apoyo manual consistente hasta que la habilidad esté técnicamente consolidada', true, 2),
    (currval('question_id_seq') - 3, 'Usar colchones gruesos como único medio de seguridad', false, 3),
    (currval('question_id_seq') - 3, 'Corregir verbalmente durante la ejecución de la habilidad', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'Movilización articular y activación de la cadena cinética', false, 1),
    (currval('question_id_seq') - 2, 'Ejercicios de elongación estática prolongada al inicio de la sesión', true, 2),
    (currval('question_id_seq') - 2, 'Trabajo cardiovascular progresivo para elevar temperatura corporal', false, 3),
    (currval('question_id_seq') - 2, 'Activación del core y hombros con ejercicios específicos', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, 'Retroalimentación general y poco frecuente para no saturar al atleta', false, 1),
    (currval('question_id_seq') - 1, 'Retroalimentación específica, inmediata y focalizada en uno o dos puntos clave', true, 2),
    (currval('question_id_seq') - 1, 'Retroalimentación únicamente positiva para mantener la motivación', false, 3),
    (currval('question_id_seq') - 1, 'Retroalimentación diferida varios días después de la ejecución', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'Cuando el atleta lo solicita', false, 1),
    (currval('question_id_seq'), 'Cuando la técnica es consistente, el atleta tiene confianza y puede ejecutar sin fallo', true, 2),
    (currval('question_id_seq'), 'Después de la primera ejecución exitosa', false, 3),
    (currval('question_id_seq'), 'Cuando el entrenador se fatiga de asistir', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 2: ARTISTICA — Gimnasia Artística Femenina (WAG)
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('ARTISTICA', 'Gimnasia Artística Femenina (WAG)', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuáles son los cuatro aparatos del programa olímpico de gimnasia artística femenina?', 1),
    (currval('skill_test_id_seq'), '¿Qué compone la nota final de una gimnasta en el código de puntuación FIG?', 2),
    (currval('skill_test_id_seq'), '¿Cuál es el requisito de composición mínimo en el ejercicio de suelo WAG?', 3),
    (currval('skill_test_id_seq'), '¿Qué penalización se aplica por una caída desde el aparato?', 4),
    (currval('skill_test_id_seq'), '¿Qué evalúa la puntuación de Ejecución (E) en el código FIG?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'Salto, barras paralelas, barra fija y suelo', false, 1),
    (currval('question_id_seq') - 4, 'Salto, barras asimétricas, barra de equilibrio y suelo', true, 2),
    (currval('question_id_seq') - 4, 'Salto, barras asimétricas, anillas y suelo', false, 3),
    (currval('question_id_seq') - 4, 'Salto, barra de equilibrio, barras paralelas y suelo', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'Nota artística y nota atlética', false, 1),
    (currval('question_id_seq') - 3, 'Puntuación D (Dificultad) + Puntuación E (Ejecución)', true, 2),
    (currval('question_id_seq') - 3, 'Puntuación técnica y puntuación coreográfica', false, 3),
    (currval('question_id_seq') - 3, 'Nota de salida más bono de dificultad', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'Incluir al menos 4 elementos de dificultad en la primera mitad', false, 1),
    (currval('question_id_seq') - 2, 'Incluir habilidades de al menos dos grupos de elementos diferentes', true, 2),
    (currval('question_id_seq') - 2, 'Ejecutar el ejercicio sin acompañamiento musical', false, 3),
    (currval('question_id_seq') - 2, 'Comenzar con una salida de al menos valor B', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, '0.5 puntos de deducción', false, 1),
    (currval('question_id_seq') - 1, '1.0 punto de deducción', true, 2),
    (currval('question_id_seq') - 1, '0.3 puntos de deducción', false, 3),
    (currval('question_id_seq') - 1, '2.0 puntos de deducción', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'El nivel de dificultad de los elementos acrobáticos', false, 1),
    (currval('question_id_seq'), 'Los errores técnicos, posturales y de forma durante la ejecución', true, 2),
    (currval('question_id_seq'), 'La expresividad artística y la musicalidad', false, 3),
    (currval('question_id_seq'), 'El tiempo de ejercicio y la composición coreográfica', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 3: ARTISTICA — Gimnasia Artística Masculina (MAG)
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('ARTISTICA', 'Gimnasia Artística Masculina (MAG)', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuáles son los seis aparatos del programa olímpico de gimnasia artística masculina?', 1),
    (currval('skill_test_id_seq'), '¿Qué caracteriza técnicamente un buen balanceo en las anillas?', 2),
    (currval('skill_test_id_seq'), '¿Cuál es el requisito mínimo de tiempo en la posición de fuerza (riesener) en anillas?', 3),
    (currval('skill_test_id_seq'), '¿Qué deducción se aplica en MAG por un paso al aterrizar la salida?', 4),
    (currval('skill_test_id_seq'), '¿Qué evalúa la nota D (Dificultad) en el código FIG masculino?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'Suelo, salto, barras paralelas, barra fija, anillas y caballo con arcos', true, 1),
    (currval('question_id_seq') - 4, 'Suelo, salto, barras asimétricas, barra fija, anillas y caballo con arcos', false, 2),
    (currval('question_id_seq') - 4, 'Suelo, salto, barras paralelas, barra de equilibrio, anillas y caballo con arcos', false, 3),
    (currval('question_id_seq') - 4, 'Suelo, trampolín, barras paralelas, barra fija, anillas y caballo con arcos', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'Las anillas deben moverse libremente para generar amplitud en el balanceo', false, 1),
    (currval('question_id_seq') - 3, 'Las anillas deben mantenerse estables (inmóviles) durante todo el ejercicio', true, 2),
    (currval('question_id_seq') - 3, 'El balanceo en anillas es aceptable si se controla al final', false, 3),
    (currval('question_id_seq') - 3, 'Pequeños movimientos de anillas no generan deducción', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'No hay requisito de tiempo; basta con alcanzar la posición', false, 1),
    (currval('question_id_seq') - 2, '2 segundos de parada estática para recibir el valor del elemento', true, 2),
    (currval('question_id_seq') - 2, '1 segundo de parada estática', false, 3),
    (currval('question_id_seq') - 2, '3 segundos de parada estática', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, '0.1 punto por paso', true, 1),
    (currval('question_id_seq') - 1, '0.3 puntos por paso', false, 2),
    (currval('question_id_seq') - 1, '0.5 puntos por paso', false, 3),
    (currval('question_id_seq') - 1, '1.0 punto por paso', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'La calidad de ejecución y la limpieza del ejercicio', false, 1),
    (currval('question_id_seq'), 'El valor de los elementos individuales y los bonos de conexión', true, 2),
    (currval('question_id_seq'), 'El valor artístico y coreográfico del ejercicio', false, 3),
    (currval('question_id_seq'), 'La dificultad de la salida únicamente', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 4: ARTISTICA — Juicio y Arbitraje en Gimnasia Artística
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('ARTISTICA', 'Juicio y Arbitraje en Gimnasia Artística', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuántos jueces E (Ejecución) conforman el panel de jueces en una competencia FIG de categoría A?', 1),
    (currval('skill_test_id_seq'), '¿Qué procedimiento se aplica cuando un equipo impugna la nota D de una gimnasta?', 2),
    (currval('skill_test_id_seq'), '¿Cuál es la función del juez de referencia (Reference Judge) en el sistema FIG?', 3),
    (currval('skill_test_id_seq'), '¿Qué deducción aplican los jueces de línea en suelo cuando la gimnasta sale del tapiz?', 4),
    (currval('skill_test_id_seq'), '¿Qué panel de jueces determina la nota D en el sistema actual de puntuación FIG?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, '4 jueces E', false, 1),
    (currval('question_id_seq') - 4, '6 jueces E', true, 2),
    (currval('question_id_seq') - 4, '3 jueces E', false, 3),
    (currval('question_id_seq') - 4, '8 jueces E', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'El árbitro superior decide sin revisión adicional', false, 1),
    (currval('question_id_seq') - 3, 'Se activa una revisión técnica donde el panel D revisa el video y puede corregir la nota', true, 2),
    (currval('question_id_seq') - 3, 'La nota D se promedia con la nota propuesta por el equipo', false, 3),
    (currval('question_id_seq') - 3, 'La impugnación no es posible en competencias FIG', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'Evalúa la dificultad de los elementos junto al panel D', false, 1),
    (currval('question_id_seq') - 2, 'Supervisa que el proceso de evaluación cumpla las normas FIG y media en disputas entre paneles', true, 2),
    (currval('question_id_seq') - 2, 'Califica la componente artística del ejercicio', false, 3),
    (currval('question_id_seq') - 2, 'Controla el tiempo de ejercicio en suelo y barra', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, '0.1 punto por salida de tapiz', true, 1),
    (currval('question_id_seq') - 1, '0.3 puntos por salida de tapiz', false, 2),
    (currval('question_id_seq') - 1, '0.5 puntos por salida de tapiz', false, 3),
    (currval('question_id_seq') - 1, 'No hay deducción por salida de tapiz', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'El panel E (Ejecución)', false, 1),
    (currval('question_id_seq'), 'El panel D (Dificultad), compuesto por 2 jueces D', true, 2),
    (currval('question_id_seq'), 'Un único juez árbitro supervisor', false, 3),
    (currval('question_id_seq'), 'Los 6 jueces E por mayoría de votos', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 5: RITMICA — Gimnasia Rítmica
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('RITMICA', 'Gimnasia Rítmica', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuáles son los cinco aparatos utilizados en gimnasia rítmica individual de nivel olímpico?', 1),
    (currval('skill_test_id_seq'), '¿Qué componentes integran la nota D (Dificultad) en el código de puntuación de gimnasia rítmica FIG?', 2),
    (currval('skill_test_id_seq'), '¿Qué aspecto técnico diferencia una gran onda corporal (body wave) de un balanceo de tronco?', 3),
    (currval('skill_test_id_seq'), '¿Cuál es la duración máxima de un ejercicio de conjuntos (5 gimnastas) en competencia internacional?', 4),
    (currval('skill_test_id_seq'), '¿Qué deducción se aplica cuando la gimnasta pierde el aparato fuera del área de competencia?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'Aro, pelota, mazas, cinta y cuerda', true, 1),
    (currval('question_id_seq') - 4, 'Aro, pelota, bastón, cinta y cuerda', false, 2),
    (currval('question_id_seq') - 4, 'Aro, pelota, mazas, pañuelo y cuerda', false, 3),
    (currval('question_id_seq') - 4, 'Aro, pelota, mazas, cinta y pañuelo', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'Solo la dificultad corporal (DB)', false, 1),
    (currval('question_id_seq') - 3, 'Dificultad corporal (DB), dificultad de aparato (DA) y composición (CL)', true, 2),
    (currval('question_id_seq') - 3, 'Solo la dificultad de aparato y la artística', false, 3),
    (currval('question_id_seq') - 3, 'Técnica de aparato y técnica corporal por separado', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'La onda corporal involucra solo la columna lumbar; el balanceo involucra todo el cuerpo', false, 1),
    (currval('question_id_seq') - 2, 'La onda corporal es un movimiento secuencial que recorre toda la columna vertebral de forma continua; el balanceo es un movimiento de péndulo desde la cadera', true, 2),
    (currval('question_id_seq') - 2, 'Son dos nombres distintos para el mismo movimiento', false, 3),
    (currval('question_id_seq') - 2, 'La onda corporal solo puede ejecutarse en posición de frente al jurado', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, '2 minutos 30 segundos', false, 1),
    (currval('question_id_seq') - 1, '2 minutos 15 segundos a 2 minutos 30 segundos', true, 2),
    (currval('question_id_seq') - 1, '1 minuto 30 segundos', false, 3),
    (currval('question_id_seq') - 1, '3 minutos', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), '0.3 puntos de penalización', false, 1),
    (currval('question_id_seq'), '1.0 punto de penalización aplicado por los jueces de árbitro', true, 2),
    (currval('question_id_seq'), '0.5 puntos de penalización', false, 3),
    (currval('question_id_seq'), 'No hay penalización si la gimnasta recupera el aparato', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 6: METODOLOGIA — Metodología del Coaching Deportivo
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('METODOLOGIA', 'Metodología del Coaching Deportivo', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Qué principio metodológico describe el aumento gradual de la carga de entrenamiento a lo largo de un ciclo?', 1),
    (currval('skill_test_id_seq'), '¿Cuál es la diferencia entre un objetivo de resultado y un objetivo de proceso en el entrenamiento?', 2),
    (currval('skill_test_id_seq'), '¿Qué es el principio de individualización en el entrenamiento deportivo?', 3),
    (currval('skill_test_id_seq'), '¿Cuál de los siguientes describe mejor la periodización en el entrenamiento de gymnastas?', 4),
    (currval('skill_test_id_seq'), '¿Qué tipo de práctica es más efectiva para la consolidación de una habilidad motriz compleja?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'Principio de especificidad', false, 1),
    (currval('question_id_seq') - 4, 'Principio de progresividad (sobrecarga progresiva)', true, 2),
    (currval('question_id_seq') - 4, 'Principio de reversibilidad', false, 3),
    (currval('question_id_seq') - 4, 'Principio de variedad', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'Son equivalentes; ambos miden el desempeño final del atleta', false, 1),
    (currval('question_id_seq') - 3, 'El objetivo de resultado se enfoca en ganar/clasificar; el de proceso, en acciones controlables por el atleta', true, 2),
    (currval('question_id_seq') - 3, 'El objetivo de proceso lo define el juez; el de resultado lo define el entrenador', false, 3),
    (currval('question_id_seq') - 3, 'Solo los objetivos de resultado son medibles en deporte de alto rendimiento', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'Aplicar el mismo plan de entrenamiento a todos los atletas del grupo', false, 1),
    (currval('question_id_seq') - 2, 'Adaptar la carga, los métodos y los objetivos a las características únicas de cada atleta', true, 2),
    (currval('question_id_seq') - 2, 'Entrenar individualmente (sin grupo) a cada atleta', false, 3),
    (currval('question_id_seq') - 2, 'Modificar el reglamento para cada atleta según su nivel', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, 'Entrenar con la misma intensidad durante todo el año', false, 1),
    (currval('question_id_seq') - 1, 'Organizar el entrenamiento en ciclos (macrociclo, mesociclo, microciclo) con fases de preparación, competencia y recuperación', true, 2),
    (currval('question_id_seq') - 1, 'Aumentar solo el volumen de entrenamiento antes de cada competencia', false, 3),
    (currval('question_id_seq') - 1, 'Reducir el entrenamiento técnico en favor del físico durante toda la temporada', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'Práctica masiva con muchas repeticiones sin descanso para maximizar el tiempo de práctica', false, 1),
    (currval('question_id_seq'), 'Práctica distribuida con variabilidad contextual para facilitar la transferencia y la retención', true, 2),
    (currval('question_id_seq'), 'Práctica exclusivamente en condiciones idénticas a la competencia desde el inicio', false, 3),
    (currval('question_id_seq'), 'Práctica mental sin componente físico hasta que la habilidad esté visualizada perfectamente', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 7: METODOLOGIA — Liderazgo del Head Coach
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('METODOLOGIA', 'Liderazgo del Head Coach', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuál es el enfoque más efectivo de un Head Coach para dar retroalimentación a otro entrenador de su staff?', 1),
    (currval('skill_test_id_seq'), '¿Cómo debe un Head Coach gestionar un conflicto entre dos atletas del equipo?', 2),
    (currval('skill_test_id_seq'), '¿Qué caracteriza a un entorno de entrenamiento psicológicamente seguro?', 3),
    (currval('skill_test_id_seq'), '¿Cuál es la responsabilidad principal del Head Coach respecto al bienestar de los atletas menores de edad?', 4),
    (currval('skill_test_id_seq'), '¿Qué habilidad de comunicación es más crítica para que un Head Coach influya positivamente en su equipo?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'Criticar públicamente para que todos aprendan del error', false, 1),
    (currval('question_id_seq') - 4, 'Dar retroalimentación específica, en privado, basada en comportamientos observables y orientada al desarrollo', true, 2),
    (currval('question_id_seq') - 4, 'Evitar la retroalimentación negativa para mantener el clima positivo', false, 3),
    (currval('question_id_seq') - 4, 'Delegar toda la retroalimentación al director técnico de la federación', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'Ignorar el conflicto esperando que se resuelva solo con el tiempo', false, 1),
    (currval('question_id_seq') - 3, 'Escuchar a cada atleta por separado, identificar la raíz del conflicto y mediar hacia una solución colaborativa', true, 2),
    (currval('question_id_seq') - 3, 'Sancionar a ambos atletas de igual forma para ser imparcial', false, 3),
    (currval('question_id_seq') - 3, 'Involucrar a los padres inmediatamente para que resuelvan el conflicto', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'Los atletas sienten miedo a equivocarse porque los errores tienen consecuencias severas', false, 1),
    (currval('question_id_seq') - 2, 'Los atletas pueden expresar dudas, cometer errores y dar su opinión sin temor a represalias', true, 2),
    (currval('question_id_seq') - 2, 'El entrenador protege al equipo de toda crítica externa', false, 3),
    (currval('question_id_seq') - 2, 'Solo los atletas de mayor nivel tienen voz en las decisiones de entrenamiento', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, 'Maximizar horas de entrenamiento para acelerar el desarrollo técnico', false, 1),
    (currval('question_id_seq') - 1, 'Garantizar un entorno seguro, libre de abuso, con supervisión adecuada y comunicación abierta con padres y tutores', true, 2),
    (currval('question_id_seq') - 1, 'Delegar el bienestar psicológico exclusivamente al psicólogo deportivo', false, 3),
    (currval('question_id_seq') - 1, 'Mantener al atleta enfocado solo en el rendimiento y evitar distracciones externas', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'Hablar con autoridad y firmeza para imponer decisiones rápidamente', false, 1),
    (currval('question_id_seq'), 'Escucha activa: comprender antes de responder, mostrando empatía y validando las perspectivas del equipo', true, 2),
    (currval('question_id_seq'), 'Usar lenguaje técnico avanzado para demostrar expertise', false, 3),
    (currval('question_id_seq'), 'Comunicarse principalmente por escrito para tener todo documentado', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 8: GESTION — Atención al Cliente y Comunicación
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('GESTION', 'Atención al Cliente y Comunicación', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuál es la respuesta más apropiada cuando un padre reclama de forma agresiva por la evaluación de su hijo?', 1),
    (currval('skill_test_id_seq'), '¿Qué técnica de comunicación reduce más eficazmente los malentendidos con las familias?', 2),
    (currval('skill_test_id_seq'), '¿Con qué frecuencia mínima se recomienda comunicar el progreso individual de un atleta a su familia?', 3),
    (currval('skill_test_id_seq'), '¿Qué elemento es esencial en una respuesta escrita a una queja formal de un cliente?', 4),
    (currval('skill_test_id_seq'), '¿Qué diferencia a la comunicación asertiva de la comunicación agresiva?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'Responder con el mismo tono para demostrar firmeza', false, 1),
    (currval('question_id_seq') - 4, 'Escuchar activamente, reconocer la emoción del padre, proponer hablar en privado y ofrecer una solución concreta', true, 2),
    (currval('question_id_seq') - 4, 'Ignorar el reclamo hasta que el padre se calme por su cuenta', false, 3),
    (currval('question_id_seq') - 4, 'Derivar inmediatamente a la dirección sin escuchar al padre', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'Usar jerga técnica para demostrar expertise', false, 1),
    (currval('question_id_seq') - 3, 'Confirmar la comprensión mediante preguntas de verificación y parafraseo', true, 2),
    (currval('question_id_seq') - 3, 'Enviar toda comunicación exclusivamente por correo electrónico', false, 3),
    (currval('question_id_seq') - 3, 'Hablar lo menos posible para evitar malentendidos', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'Solo cuando hay un problema o incidente', false, 1),
    (currval('question_id_seq') - 2, 'Al menos una vez por trimestre, con reporte estructurado de logros y áreas de mejora', true, 2),
    (currval('question_id_seq') - 2, 'Al inicio y final del año, sin comunicaciones intermedias', false, 3),
    (currval('question_id_seq') - 2, 'La comunicación de progreso depende exclusivamente de la solicitud del padre', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, 'Una disculpa genérica sin referencia específica al reclamo', false, 1),
    (currval('question_id_seq') - 1, 'Acuse de recibo, resumen del problema comprendido, las acciones tomadas y el seguimiento previsto', true, 2),
    (currval('question_id_seq') - 1, 'Una explicación detallada de por qué el cliente está equivocado', false, 3),
    (currval('question_id_seq') - 1, 'Una respuesta breve prometiendo revisar sin dar plazos', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'La asertiva usa tono elevado para hacerse escuchar; la agresiva es más suave', false, 1),
    (currval('question_id_seq'), 'La asertiva expresa necesidades propias respetando los derechos del otro; la agresiva impone sin considerar al otro', true, 2),
    (currval('question_id_seq'), 'Son sinónimos; ambas buscan defender los derechos propios', false, 3),
    (currval('question_id_seq'), 'La asertiva evita el conflicto cediendo; la agresiva genera conflicto', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 9: GESTION — Ventas e Inscripciones
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('GESTION', 'Ventas e Inscripciones', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuál es el primer paso en un proceso de ventas consultivo para inscripciones en un club de gimnasia?', 1),
    (currval('skill_test_id_seq'), '¿Qué métrica indica mejor la eficiencia del proceso de inscripción de un club?', 2),
    (currval('skill_test_id_seq'), '¿Cómo se maneja mejor una objeción de precio durante el proceso de inscripción?', 3),
    (currval('skill_test_id_seq'), '¿Qué estrategia de seguimiento es más efectiva tras una consulta de inscripción no concretada?', 4),
    (currval('skill_test_id_seq'), '¿Cuál es el beneficio principal de implementar un sistema de CRM en un club de gimnasia?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'Presentar inmediatamente los precios y planes disponibles', false, 1),
    (currval('question_id_seq') - 4, 'Hacer preguntas para entender las necesidades, objetivos y situación del cliente potencial', true, 2),
    (currval('question_id_seq') - 4, 'Ofrecer un descuento de bienvenida para cerrar rápido', false, 3),
    (currval('question_id_seq') - 4, 'Mostrar las instalaciones antes de hablar con el cliente', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'Número total de consultas recibidas por mes', false, 1),
    (currval('question_id_seq') - 3, 'Tasa de conversión: porcentaje de consultas que se convierten en inscripciones', true, 2),
    (currval('question_id_seq') - 3, 'Número de publicaciones en redes sociales del mes', false, 3),
    (currval('question_id_seq') - 3, 'Tiempo promedio de respuesta a un mensaje de WhatsApp', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'Reducir el precio inmediatamente para cerrar la venta', false, 1),
    (currval('question_id_seq') - 2, 'Reconocer la objeción, reforzar el valor diferencial y ofrecer opciones de planes si existen', true, 2),
    (currval('question_id_seq') - 2, 'Ignorar la objeción y seguir presentando características del programa', false, 3),
    (currval('question_id_seq') - 2, 'Comparar el precio con el de la competencia para justificarlo', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, 'No hacer seguimiento para no presionar al cliente', false, 1),
    (currval('question_id_seq') - 1, 'Contactar dentro de las 48 horas con información personalizada relevante a sus necesidades expresadas', true, 2),
    (currval('question_id_seq') - 1, 'Enviar la lista de precios completa por email al día siguiente', false, 3),
    (currval('question_id_seq') - 1, 'Esperar a que el cliente contacte nuevamente cuando esté listo', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'Automatizar el cobro de cuotas mensuales', false, 1),
    (currval('question_id_seq'), 'Centralizar la historia de cada cliente para personalizar la comunicación y mejorar la retención', true, 2),
    (currval('question_id_seq'), 'Eliminar la necesidad de personal administrativo', false, 3),
    (currval('question_id_seq'), 'Publicar automáticamente contenido en redes sociales', false, 4);


-- ──────────────────────────────────────────────────────────
-- TEST 10: GESTION — Administración de Club de Gimnasia
-- ──────────────────────────────────────────────────────────
INSERT INTO skill_test (skill, title, active, duration_minutes) VALUES
    ('GESTION', 'Administración de Club de Gimnasia', true, 10);

INSERT INTO question (skill_test_id, text, position) VALUES
    (currval('skill_test_id_seq'), '¿Cuál es el documento fundamental para establecer los derechos y obligaciones entre el club y las familias?', 1),
    (currval('skill_test_id_seq'), '¿Qué indicador financiero permite saber si el club puede cubrir sus gastos operativos mes a mes?', 2),
    (currval('skill_test_id_seq'), '¿Cómo se calcula el punto de equilibrio (break-even) de un club de gimnasia?', 3),
    (currval('skill_test_id_seq'), '¿Qué práctica reduce más el riesgo de accidentes y la responsabilidad legal del club?', 4),
    (currval('skill_test_id_seq'), '¿Cuál es la principal ventaja de estructurar el club como persona jurídica (asociación o sociedad)?', 5);

-- Q1
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 4, 'El reglamento interno del staff de entrenadores', false, 1),
    (currval('question_id_seq') - 4, 'El contrato de inscripción o reglamento firmado por los tutores del atleta', true, 2),
    (currval('question_id_seq') - 4, 'El plan de entrenamiento anual del gimnasta', false, 3),
    (currval('question_id_seq') - 4, 'La planilla de asistencia mensual', false, 4);

-- Q2
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 3, 'El EBITDA anual', false, 1),
    (currval('question_id_seq') - 3, 'El flujo de caja operativo mensual (ingresos por cuotas menos gastos fijos y variables)', true, 2),
    (currval('question_id_seq') - 3, 'La rentabilidad sobre el patrimonio (ROE)', false, 3),
    (currval('question_id_seq') - 3, 'El índice de endeudamiento total del club', false, 4);

-- Q3
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 2, 'Ingresos totales dividido por el número de atletas', false, 1),
    (currval('question_id_seq') - 2, 'Costos fijos totales dividido por el margen de contribución unitario (precio cuota − costo variable por atleta)', true, 2),
    (currval('question_id_seq') - 2, 'Ganancias del año anterior multiplicadas por la inflación proyectada', false, 3),
    (currval('question_id_seq') - 2, 'Número de atletas necesarios para pagar solo los sueldos del staff', false, 4);

-- Q4
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq') - 1, 'Contratar un abogado disponible para emergencias', false, 1),
    (currval('question_id_seq') - 1, 'Mantener protocolos escritos de seguridad, inspecciones regulares de equipos, seguro de responsabilidad civil y capacitación en primeros auxilios del staff', true, 2),
    (currval('question_id_seq') - 1, 'Solicitar a las familias firmar una renuncia total de responsabilidad', false, 3),
    (currval('question_id_seq') - 1, 'Reducir las actividades de mayor riesgo técnico del programa', false, 4);

-- Q5
INSERT INTO option (question_id, text, is_correct, position) VALUES
    (currval('question_id_seq'), 'Permite acceder a subsidios deportivos automáticamente', false, 1),
    (currval('question_id_seq'), 'Separa el patrimonio personal del fundador del patrimonio del club, limitando la responsabilidad personal', true, 2),
    (currval('question_id_seq'), 'Elimina la necesidad de llevar contabilidad', false, 3),
    (currval('question_id_seq'), 'Garantiza exención de impuestos en todos los países', false, 4);
