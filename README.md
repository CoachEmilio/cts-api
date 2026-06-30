# CTS — Coach Talent System

Backend en **Java 21 + Spring Boot 3** para una plataforma donde la comunidad de
gimnasia arma su perfil de trabajo con **habilidades verificadas por test**. Un
candidato (coach) rinde un test de una habilidad y obtiene un score honesto;
existen varios tests, y el banco de preguntas es editable de punta a punta.

## Qué es y qué no es

**Es:** un SaaS de perfiles con skill verificado para el nicho de gimnasia. La
diferencia no es el alcance ni el pipeline: es que la habilidad está probada por
un test, no autodeclarada en un CV.

**No es:** ni un ATS con Kanban, ni una bolsa de trabajo, ni un LinkedIn. No
parsea CVs en PDF ni usa IA para puntuar. Esa complejidad se descartó a propósito.

## El trust boundary

El servidor es la autoridad del score. Tres reglas irrompibles:

1. Las respuestas correctas (`isCorrect`) nunca salen del backend.
2. El score lo calcula el servidor a partir de respuestas crudas. El cliente nunca manda un puntaje.
3. El servidor decide qué intento es válido.

## Scoring: plano

```
score = respuestas_correctas / total_preguntas  (0–100%)
```

Vive detrás de `ScoringStrategy` (una sola impl plana hoy — OCP listo para
extensión sin tocar el motor).

## Estado: M1–M8 completados

| Milestone | Estado | Qué entrega |
|-----------|--------|-------------|
| M1 | ✅ | Banco editable (CRUD tests/preguntas/opciones) + vista saneada para candidatos (`isCorrect` nunca sale) |
| M2 | ✅ | Iniciar intento, responder, submit, scoring server-side (`scorePct` como `NUMERIC(5,2)`) |
| M3 | ✅ | Auth JWT + roles (CANDIDATE / ADMIN / RECRUITER) + `@PreAuthorize` en todos los endpoints |
| M4 | ✅ | Perfil del candidato (upsert) + historial de skills verificadas (`GET /users/me/skill-history`) |
| M5 | ✅ | Testcontainers (singleton pattern), MockMvc integration tests, Dockerfile multi-stage, docker-compose, GitHub Actions CI |
| M6 | ✅ | Descubrimiento recruiter: `GET /api/v1/recruiter/candidates?skill=X&minScore=Y` |
| M7 | ✅ | Onboarding/perfil: campos extra en registro, `onboardingComplete`, `GET/PATCH /users/me`, general-information, upload de avatar |
| M8 | ✅ | Anti-trampa: deadline server-side, one-attempt gate, registro de violaciones, `completed` por test, DELETE test |

**80 tests pasan.** Suite completa en `./mvnw verify`.

## Skills disponibles

`ACROBACIA` · `ARTISTICA` · `RITMICA` · `TRAMPOLIN` · `TUMBLING` · `AEROBICA` · `TELA` · `METODOLOGIA` · `GESTION`

Case-insensitive en entrada, lowercase en respuesta JSON. Agregar un valor nuevo requiere migración Flyway que actualice el CHECK constraint.

## Tests seed (10 tests, 50 preguntas)

Cargados vía la API como admin. Distribución por skill:

| Skill | Tests |
|-------|-------|
| ACROBACIA | Foundations of Gymnastics Coaching |
| ARTISTICA | WAG — Women's Artistic Gymnastics, MAG — Men's Artistic Gymnastics, Judging — Women's Artistic Gymnastics |
| RITMICA | Rhythmic Gymnastics |
| METODOLOGIA | Coaching Methodology, Head Coach Leadership |
| GESTION | Customer Service & Communication, Sales & Enrollment, Gymnastics Club Administration |

## Contrato de la API

Auth: JWT Bearer. Roles: `CANDIDATE`, `ADMIN`, `RECRUITER`. Errores:
`ProblemDetail` (RFC 7807). Sin envelopes. Role serializado como string lowercase
(`"candidate"`, `"admin"`, `"recruiter"`).

```
# Auth (público)
POST   /api/v1/auth/register          → { token, role, onboardingComplete }
POST   /api/v1/auth/login             → { token, role, onboardingComplete }

# Usuario actual (autenticado)
GET    /api/v1/users/me               → { id, email, fullName, phone, role, onboardingComplete, avatarUrl }
PATCH  /api/v1/users/me               → actualiza onboardingComplete, avatarUrl, fullName, phone

# Banco editable (ADMIN)
POST   /api/v1/tests                  → { skill, title, active, durationMinutes? }
PUT    /api/v1/tests/{id}
DELETE /api/v1/tests/{id}
POST   /api/v1/tests/{id}/questions
PUT    /api/v1/questions/{id}
DELETE /api/v1/questions/{id}

# Tests para candidatos (autenticado)
GET    /api/v1/tests                  → lista activos, sin isCorrect (incluye durationMinutes y completed)
GET    /api/v1/tests/{id}             → test saneado

# Rendir test (CANDIDATE)
POST   /api/v1/tests/{id}/attempts    → inicia intento → { attemptId, testId, status, startedAt, deadline }
                                        409 si ya existe un intento SUBMITTED para este test
POST   /api/v1/attempts/{id}/answers  → responde/cambia respuesta
POST   /api/v1/attempts/{id}/submit   → calcula score server-side → Result
POST   /api/v1/attempts/{id}/violations → registra una violación de anti-trampa (204)

# Perfil candidato (CANDIDATE)
GET    /api/v1/users/me/profile
PUT    /api/v1/users/me/profile
GET    /api/v1/users/me/skill-history → historial de intentos submitted con score

# General Information / perfil (autenticado)
GET    /api/v1/general-information    → categorías + preguntas + opciones del form de perfil
POST   /api/v1/general-information    → guarda respuestas seleccionadas del candidato

# Upload (autenticado)
POST   /api/v1/upload                 → multipart/form-data → { url }
GET    /uploads/{filename}            → recurso público (sirve avatares)

# Recruiter (RECRUITER)
GET    /api/v1/recruiter/candidates?skill=X&minScore=Y  → candidatos con score verificado
```

## Anti-trampa (M8)

El intento tiene un **deadline calculado server-side** (`startedAt + durationMinutes`).
El cliente recibe el `deadline` en ISO-8601 al iniciar y hace el countdown localmente.

Eventos que generan una violación (frontend):
- Cambio de pestaña (`visibilitychange`)
- Pérdida de foco de ventana (`blur`)
- Salida de pantalla completa (`fullscreenchange`)

Cada violación se persiste vía `POST /attempts/{id}/violations`. El frontend acumula
hasta 3; a la tercera hace auto-submit. Al vencimiento del deadline, el frontend
también hace auto-submit.

El campo `violations_count` en `attempt` es auditoría — no se usa para rechazar
el intento server-side (la política de 3 violaciones es UI).

Un candidato **no puede rendir el mismo test dos veces**: si ya existe un intento
`SUBMITTED` para ese `(user, skillTest)`, el endpoint devuelve 409.

`GET /api/v1/tests` incluye `completed: boolean` por test, calculado server-side
según si el usuario autenticado ya tiene un intento `SUBMITTED`.

## Modelo de dominio

```
identity:   AppUser(email, fullName, phone, role, plan, onboardingComplete, avatarUrl)
            CandidateProfile(displayName, bio)
assessment: SkillTest(skill, title, active, durationMinutes) → Question → Option(isCorrect, solo-servidor)
            SkillTestCandidateView incluye completed:boolean calculado server-side por usuario
attempt:    Attempt(user, skillTest, status, startedAt, submittedAt, deadline, violationsCount)
            → Answer → Result(scorePct, correctCount, totalCount)
scoring:    ScoringStrategy, FlatScoringStrategy
onboarding: GeneralInfoCategory → GeneralInfoQuestion → GeneralInfoOption → UserGeneralInfoAnswer
recruiter:  búsqueda de candidatos por skill/score (vista sobre Result)
```

## Stack

| Tecnología | Versión |
|------------|---------|
| Java | 21 (LTS) |
| Spring Boot | 3.3.5 |
| Spring Security + JJWT | 0.12.6 |
| Spring Data JPA / Hibernate | 6.5 |
| PostgreSQL | 16 |
| Flyway | (incluido en Boot) |
| springdoc-openapi | 2.6.0 |
| Testcontainers | (incluido en spring-boot-testcontainers) |
| JUnit 5 + Mockito | (incluido en Boot) |
| Maven Wrapper | 3.9.9 |

## Cómo correr

```bash
# Levantar PostgreSQL en puerto 5433
docker compose up -d db

# Correr la app
./mvnw spring-boot:run

# Tests (Testcontainers levanta PostgreSQL automáticamente)
./mvnw verify

# Build completo con Docker
docker compose up --build
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API docs: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Usuario admin (desarrollo)

Creado por la migración V9. No hay forma de registrar un ADMIN por el endpoint
público — el rol sólo se asigna directamente en BD o via seed.

| Campo | Valor |
|-------|-------|
| Email | `admin@cts.dev` |
| Password | `admin123` |
| Rol | `ADMIN` |

## Estructura de paquetes

```
com.surstudio.cts
├── identity        # AppUser, CandidateProfile, auth, perfil, users/me
│   ├── api/        # AuthController, ProfileController, UsersController
│   ├── application/# AuthService, ProfileService
│   ├── domain/     # AppUser, CandidateProfile, Role, Plan, repos
│   └── dto/
├── assessment      # Banco editable + vista saneada
│   ├── api/        # SkillTestController
│   ├── application/# SkillTestService
│   ├── domain/     # SkillTest, Skill (enum), Question, Option, repos
│   └── dto/
├── attempt         # Rendir test, scoring, historial
│   ├── api/        # AttemptController
│   ├── application/# AttemptService
│   ├── domain/     # Attempt, Answer, Result, repos
│   └── dto/
├── scoring         # ScoringStrategy (interfaz) + FlatScoringStrategy
├── onboarding      # General Information (perfil profesional del candidato)
│   ├── api/        # GeneralInfoController
│   ├── application/# OnboardingService
│   ├── domain/     # GeneralInfoCategory/Question/Option, UserGeneralInfoAnswer, repos
│   └── dto/
├── recruiter       # Descubrimiento de candidatos por skill/score
│   ├── api/        # RecruiterController
│   ├── application/# RecruiterService
│   └── dto/        # CandidateMatchDto
└── common          # GlobalExceptionHandler, excepciones, security, upload
    ├── security/   # JwtService, JwtAuthFilter, SecurityConfig, UserDetailsConfig
    └── upload/     # UploadController, UploadResourceConfig
```

> El package `onboarding` contiene únicamente General Information (el research
> wizard fue removido en V8).

## Migraciones Flyway

| Versión | Contenido |
|---------|-----------|
| V1 | `skill_test`, `question`, `option` |
| V2 | `attempt`, `answer`, `result` |
| V3 | `app_user` |
| V4 | `candidate_profile` + `user_id` FK en `attempt` |
| V5 | Campos onboarding en `app_user` + tablas `general_info_*` |
| V6 | Seed: opciones de información general (especialidad, experiencia, inglés) |
| V7 | `skill` como enum (CHECK constraint) |
| V8 | Remove research wizard: drop `onboarding_question/option`, `user_research_answer` |
| V9 | Seed: usuario admin de desarrollo (`admin@cts.dev` / `admin123`) |
| V10 | `duration_minutes` en `skill_test` · `deadline` y `violations_count` en `attempt` |
| V11 | Extiende CHECK constraint de `skill` con `METODOLOGIA` y `GESTION` |

## Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/cts_db` | JDBC URL de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `cts_user` | Usuario DB |
| `SPRING_DATASOURCE_PASSWORD` | `cts_pass` | Password DB |
| `APP_JWT_SECRET` | (dev key) | Secret HMAC-SHA256 para JWT (**producción: ≥ 32 chars, nunca el default**) |
| `APP_JWT_EXPIRATION` | `86400000` | Expiración JWT en ms (24 h) |
| `APP_UPLOAD_DIR` | `uploads` | Directorio local donde se guardan los avatares |
| `APP_UPLOAD_BASE_URL` | `` | Base URL pública para construir la URL del avatar |

## Decisiones / trade-offs

- **Score plano en vez de ponderado:** más simple, más honesto. `NUMERIC(5,2)` en DB, `BigDecimal` en Java.
- **`skill` como enum (no String):** vocabulario canónico, sin typos, discovery confiable. Normalización case-insensitive en el borde. Si algún día las skills tienen que ser dato administrable en runtime, se migra de enum a entidad.
- **Research wizard removido:** se recolectaba pero ningún cliente lo consumía (peso muerto). General Information se conserva.
- **Trust boundary estructural:** `SkillTestCandidateView.OptionDto` no tiene campo `correct` — imposible exponerlo accidentalmente.
- **Deadline server-side:** el cliente recibe el timestamp y hace el countdown. El servidor no rechaza el submit si se pasó el tiempo (el frontend hace auto-submit antes), pero la auditoría queda en `deadline`.
- **Violaciones como auditoría, no como gate server:** la política de 3 violaciones vive en el frontend (UX); el backend persiste el conteo para análisis pero no bloquea el submit en base a él.
- **One-attempt enforced en el servidor:** el check de `existsByUserIdAndSkillTestIdAndStatus(SUBMITTED)` está en `AttemptService`, no en el cliente.
- **`completed` server-side en la lista de tests:** `GET /api/v1/tests` calcula por cada test si el usuario ya tiene un `SUBMITTED`, devolviendo `completed: boolean`.
- **Contrato limpio (HTTP + ProblemDetail):** sin envelope. Role como lowercase string en respuestas al cliente.
- **DIP:** el dominio define los `Repository` como interfaces; Spring Data los implementa.
- **Singleton container pattern en tests:** Testcontainers sin `@Testcontainers`/`@Container` — el contenedor vive en un `static {}` para sobrevivir entre clases que comparten el contexto Spring cacheado.
- **JWT stateless + `@PreAuthorize`:** sin sesión server-side. Roles reforzados en el método, no solo en la ruta.
- **`ddl-auto: validate`:** Flyway es dueño del esquema. Hibernate solo valida.
- **Upload local:** avatares guardados en `./uploads/` y servidos como recurso estático. En producción se reemplaza el `UploadController` por S3/GCS sin cambiar el contrato.
