# CLAUDE.md — CTS API

Contexto de proyecto para Claude Code. Leelo entero antes de proponer cambios.
El detalle largo está en `README.md`; este archivo es la versión corta y las
reglas que **no se rompen**.

## Qué es

Backend en Java 21 + Spring Boot 3 para una plataforma de perfiles con
**habilidad verificada por test** para la comunidad de gimnasia. Un candidato
(coach) rinde un test de una habilidad y obtiene un score honesto. El banco de
preguntas es **editable**. NO es un ATS, ni una bolsa de trabajo, ni un LinkedIn.

## Stack y versiones

Java 21 · Spring Boot 3.3 · Spring MVC · Spring Data JPA · PostgreSQL · Flyway ·
Spring Security · Bean Validation · springdoc-openapi · Actuator ·
JUnit 5 + Mockito · Maven (wrapper). Base package: `com.surstudio.cts`.

## Comandos

```bash
docker compose up -d db        # levantar PostgreSQL (si hay compose)
./mvnw spring-boot:run         # correr la app
./mvnw test                    # tests
./mvnw verify                  # build + tests (lo que corre el CI)
```

Swagger: `http://localhost:8080/swagger-ui.html` · Health: `/actuator/health`

## Reglas de diseño NO NEGOCIABLES

1. **Trust boundary (lo más importante):** el servidor es la autoridad del score.
    - El campo `isCorrect` NUNCA sale del backend. Los DTOs que ve el candidato
      no lo incluyen.
    - El score lo calcula el servidor a partir de respuestas crudas. El cliente
      nunca manda un puntaje.
2. **Score plano:** `score = correctas / total` (0–100%). Sin pesos, sin
   secciones ponderadas. Vive detrás de `ScoringStrategy` (una sola impl plana).
3. **Contrato limpio:** éxito = recurso directo + status HTTP. Errores =
   `ProblemDetail` (RFC 7807). NO uses un envelope `{ result, data, errorCode }`.
4. **Package por feature + DIP:** el dominio define puertos (interfaces); la
   infra implementa los adapters. Spring inyecta.
5. **DTOs en el borde:** nunca exponer entidades JPA en el contrato HTTP.
6. **Flyway es dueño del esquema.** `spring.jpa.hibernate.ddl-auto: validate`.
   Todo cambio de esquema va como migración versionada, no por Hibernate.
7. **La lógica vive en services, no en controllers.** El controller solo traduce
   HTTP. Las transacciones se delimitan en la capa de servicio.

## Modelo de dominio

- **identity:** `AppUser(email, fullName, phone, role, plan, onboardingComplete, avatarUrl)`, `CandidateProfile(displayName, bio)`
- **assessment (banco editable):** `SkillTest(skill: Skill, title, active)` → `Question` → `Option(isCorrect, solo-servidor)`
- **attempt:** `Attempt(user, skillTest, status, startedAt, submittedAt)` → `Answer` → `Result(scorePct, correctCount, totalCount)`
- **scoring:** `ScoringStrategy`, `FlatScoringStrategy`
- **onboarding:** `GeneralInfoCategory` → `GeneralInfoQuestion` → `GeneralInfoOption` | `UserGeneralInfoAnswer`
- **recruiter:** búsqueda de candidatos por skill/score (vista sobre `Result`)

## Contrato de API actual (M1–M7 completos)

```
# Auth (público)
POST   /api/v1/auth/register          → { token, role, onboardingComplete }
POST   /api/v1/auth/login             → { token, role, onboardingComplete }

# Usuario actual (autenticado)
GET    /api/v1/users/me               → { id, email, fullName, phone, role, onboardingComplete, avatarUrl }
PATCH  /api/v1/users/me               → actualiza onboardingComplete, avatarUrl, fullName, phone

# Banco editable (ADMIN)
POST   /api/v1/tests
PUT    /api/v1/tests/{id}
POST   /api/v1/tests/{id}/questions
PUT    /api/v1/questions/{id}
DELETE /api/v1/questions/{id}

# Tests para candidatos (autenticado)
GET    /api/v1/tests                  → lista activos, sin isCorrect
GET    /api/v1/tests/{id}             → test saneado

# Rendir test (CANDIDATE)
POST   /api/v1/tests/{id}/attempts    → inicia intento
POST   /api/v1/attempts/{id}/answers  → responde/cambia respuesta
POST   /api/v1/attempts/{id}/submit   → calcula score server-side → Result

# Perfil candidato (CANDIDATE)
GET    /api/v1/users/me/profile
PUT    /api/v1/users/me/profile
GET    /api/v1/users/me/skill-history → historial de intentos submitted con score

# General Information (autenticado)
GET    /api/v1/general-information    → categorías + preguntas + opciones del form de perfil
POST   /api/v1/general-information    → guarda respuestas seleccionadas del candidato

# Upload (autenticado)
POST   /api/v1/upload                 → multipart/form-data → { url }
GET    /uploads/{filename}            → recurso público (sirve avatares)

# Recruiter (RECRUITER)
GET    /api/v1/recruiter/candidates?skill=X&minScore=Y  → candidatos con score verificado
```

**Skill enum:** ACROBACIA | ARTISTICA | RITMICA | TRAMPOLIN | TUMBLING | AEROBICA | TELA
(case-insensitive en entrada, lowercase en respuesta JSON)

**Errores:** ProblemDetail RFC 7807. Role serializado como lowercase (`"candidate"`, `"admin"`, `"recruiter"`).

## Migraciones Flyway

| Versión | Contenido |
|---|---|
| V1 | `skill_test`, `question`, `option` |
| V2 | `attempt`, `answer`, `result` |
| V3 | `app_user` |
| V4 | `candidate_profile` + `user_id` FK en `attempt` |
| V5 | Campos onboarding en `app_user` + tablas general_info_* + tablas research (luego eliminadas) |
| V6 | Seed: general info (especialidad, experiencia, inglés) + seed research (eliminado por V8) |
| V7 | Normaliza skill a enum + CHECK constraint en `skill_test.skill` |
| V8 | DROP tablas research wizard (onboarding_question / onboarding_option / user_research_answer) |

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
│   ├── domain/     # SkillTest, Question, Option, Skill (enum), repos
│   └── dto/
├── attempt         # Rendir test, scoring, historial
│   ├── api/        # AttemptController
│   ├── application/# AttemptService
│   ├── domain/     # Attempt, Answer, Result, repos
│   └── dto/
├── scoring         # ScoringStrategy (interfaz) + FlatScoringStrategy
├── onboarding      # Formulario de perfil profesional (General Information)
│   ├── api/        # GeneralInfoController
│   ├── application/# OnboardingService
│   ├── domain/     # GeneralInfoCategory/Question/Option, UserGeneralInfoAnswer, repos
│   └── dto/
├── recruiter       # Descubrimiento de candidatos por skill/score
│   ├── api/        # RecruiterController
│   ├── application/# RecruiterService
│   └── dto/        # CandidateMatchDto
└── common          # GlobalExceptionHandler, SkillConverter, security, upload
    ├── security/   # JwtService, JwtAuthFilter, SecurityConfig, UserDetailsConfig
    └── upload/     # UploadController, UploadResourceConfig
```

## Qué NO hacer

- No agregar ATS/Kanban/pipeline, parsing de CVs en PDF, ni scoring con IA.
- No reintroducir scoring ponderado ni secciones con peso.
- No exponer `isCorrect` al candidato bajo ninguna forma.
- No meter lógica de negocio en controllers.
- No usar `ddl-auto: update`/`create`.
- No reintroducir el research wizard (OnboardingQuestion/Option/UserResearchAnswer) —
  fue removido en el refactoring por Speculative Generality (V8).
- No agregar nuevos valores al enum `Skill` sin una migración que actualice el
  CHECK constraint en `skill_test.skill`.

## Filosofía

"Lo más chico útil, pero bien hecho." Utilidad como desempate, calidad de
portfolio como piso: todo lo que se construye va limpio, testeado y documentado.
