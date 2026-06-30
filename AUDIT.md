# Auditoría de Seguridad y Calidad — CTS API

**Fecha:** 2026-06-29  
**Revisado por:** Claude Sonnet 4.6 (automated audit)  
**Branch:** `main` — commit `9e7167b`  
**Alcance:** Código fuente completo en `src/main/java` + migraciones Flyway + suite de tests

---

## Resumen Ejecutivo

El backend implementa correctamente el trust boundary más crítico: `isCorrect` no aparece en ningún DTO de candidato y el score se calcula 100% server-side. La arquitectura general respeta DIP y DTOs en el borde. Sin embargo, se encontraron **dos vulnerabilidades que rompen el modelo de negocio anti-cheat**: el deadline calculado en `startAttempt` nunca se valida al responder ni al submit (los candidatos pueden tomarse tiempo ilimitado), y la compuerta de un-intento-por-test tiene un bypass por attempts `IN_PROGRESS` huérfanos. Además, `UsersController` inyecta el repositorio directamente y tiene `@Transactional` en los métodos del controller, una violación directa de las reglas de diseño documentadas.

---

## Hallazgos Críticos

### C-1: Deadline calculado pero nunca validado server-side

**Archivo:** `src/main/java/com/surstudio/cts/attempt/application/AttemptService.java:63-66` (cálculo) y `:146-149` (validación ausente)

**Descripción:**  
`startAttempt` calcula correctamente el deadline:
```java
var deadline = Instant.now().plusSeconds((long) test.getDurationMinutes() * 60);
attempt.setDeadline(deadline);
```
El valor se persiste en BD (V10). Pero `requireInProgress` solo verifica el status:
```java
private void requireInProgress(Attempt attempt) {
    if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
        throw new ConflictException("Attempt " + attempt.getId() + " is already submitted");
    }
    // ← NO hay verificación de attempt.getDeadline()
}
```
Este helper es llamado por `submitAnswer` (línea 79), `submitAttempt` (línea 109) y `recordViolation` (línea 71). Ninguno de los tres rechaza requests después de que expiró el deadline.

**Impacto:** El timer existe en el frontend pero es ornamental. Un candidato puede iniciar el test, cerrar el navegador, y volver días después a enviar respuestas. El mecanismo anti-cheat central está desactivado.

**Fix:**
```java
private void requireInProgress(Attempt attempt) {
    if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
        throw new ConflictException("Attempt " + attempt.getId() + " is already submitted");
    }
    if (attempt.getDeadline() != null && Instant.now().isAfter(attempt.getDeadline())) {
        // Auto-submit o rechazar
        throw new ConflictException("Attempt " + attempt.getId() + " has expired");
    }
}
```
Opcionalmente, auto-submit con score 0 en intentos expirados para no dejar attempts zombies `IN_PROGRESS`.

---

### C-2: One-attempt gate solo bloquea SUBMITTED, no IN_PROGRESS

**Archivo:** `src/main/java/com/surstudio/cts/attempt/application/AttemptService.java:57-59`

**Descripción:**
```java
if (attemptRepository.existsByUserIdAndSkillTestIdAndStatus(
        user.getId(), testId, AttemptStatus.SUBMITTED)) {
    throw new ConflictException("You have already completed this test");
}
```
Solo verifica `SUBMITTED`. Si un candidato tiene un attempt `IN_PROGRESS` (incluso uno abandonado) para el mismo test, puede abrir otro. Combinado con C-1 (deadline nunca vence los attempts), un candidato puede acumular múltiples attempts `IN_PROGRESS` para el mismo test y elegir el mejor momento o el mejor intento para submit.

**Impacto:** Viola la regla de negocio "one-attempt gate". En el escenario de explotación: candidato abre 3 attempts, estudia el banco de preguntas que ahora conoce de los 3 attempts, y luego submittea el que más le conviene.

**Fix:**
```java
// Bloquear si existe cualquier attempt no-abandonado para este test
boolean alreadyAttempted = attemptRepository
    .existsByUserIdAndSkillTestIdAndStatusIn(
        user.getId(), testId, 
        List.of(AttemptStatus.IN_PROGRESS, AttemptStatus.SUBMITTED));
if (alreadyAttempted) {
    throw new ConflictException("You have already attempted this test");
}
```
Agregar el método al repositorio:
```java
boolean existsByUserIdAndSkillTestIdAndStatusIn(Long userId, Long skillTestId, List<AttemptStatus> statuses);
```

---

### C-3: @Transactional en controller + repositorio inyectado directo en controller

**Archivo:** `src/main/java/com/surstudio/cts/identity/api/UsersController.java:13-44`

**Descripción:**  
`UsersController` inyecta `UserRepository` directamente (línea 13), y anota sus métodos con `@Transactional` y `@Transactional(readOnly = true)` (líneas 22 y 28). El PATCH además contiene lógica de negocio (aplicar el partial update campo por campo) dentro del controller.

Esto viola explícitamente CLAUDE.md reglas 4 y 7:
- "el dominio define puertos (interfaces); la infra implementa los adapters"
- "La lógica vive en services, no en controllers"

Adicionalmente, `@Transactional` en un controller (capa de presentación) tiene comportamiento inconsistente: el proxy transaccional de Spring solo intercepta llamadas externas al bean; una llamada desde el mismo controller a otro método anotado no abre una nueva transacción correctamente.

**Impacto:** Violación de arquitectura; dificulta testing unitario del controller sin BD; introduce posibles bugs de transaccionalidad en el futuro.

**Fix:** Extraer a `UserService` con los métodos `getMe(AppUser)` y `patchMe(AppUser, PatchUserRequest)`, anotados con `@Transactional`. El controller solo delega.

---

### C-4: GeneralInfoController sin autorización de rol en POST

**Archivo:** `src/main/java/com/surstudio/cts/onboarding/api/GeneralInfoController.java:15-34`

**Descripción:**  
La clase no tiene `@PreAuthorize`. El endpoint `POST /api/v1/general-information` acepta respuestas de cualquier usuario autenticado: ADMIN, RECRUITER o CANDIDATE. Un RECRUITER o ADMIN puede "completar su onboarding de candidato", mezclando estado de roles incompatibles en la misma tabla `user_general_info_answer`.

El GET es razonablemente público (cualquier rol puede ver las opciones de formulario), pero el POST debería restringirse a CANDIDATE.

**Impacto:** Violación de boundary de rol; datos de onboarding contaminados con usuarios que no son candidatos; potencial confusión al consultar respuestas en el futuro.

**Fix:**
```java
@PostMapping
@ResponseStatus(HttpStatus.NO_CONTENT)
@PreAuthorize("hasRole('CANDIDATE')")
public void saveAnswers(...) { ... }
```

---

## Hallazgos Moderados

### M-1: N+1 query en ProfileService.getSkillHistory

**Archivo:** `src/main/java/com/surstudio/cts/identity/application/ProfileService.java:54-71`

**Descripción:**  
`findByUserIdAndStatus` carga todos los attempts en una query. Luego, por cada attempt:
```java
var result = resultRepository.findByAttempt(attempt).orElseThrow();
```
Esto genera 1 + N queries (N = número de tests aprobados). El `default_batch_fetch_size: 16` del yml no ayuda aquí porque estas son llamadas explícitas al repositorio, no lazy loading de asociaciones.

**Fix:** Crear una JPQL con JOIN FETCH en `AttemptRepository` o en `ResultRepository` que retorne `Result` + `Attempt` + `SkillTest` en una sola query para el userId dado.

---

### M-2: N+1 query en RecruiterService.searchCandidates

**Archivo:** `src/main/java/com/surstudio/cts/recruiter/application/RecruiterService.java:38-43`

**Descripción:**  
La query de `ResultRepository.findBySkillAndMinScore` hace JOIN FETCH correcto (result + attempt + user + skillTest). Pero luego, por cada resultado:
```java
String displayName = profileRepository.findByUserId(user.getId()) ...
```
Genera 1 query de profiles por candidato. Con 100 candidatos matcheando = 101 queries totales.

**Fix:** LEFT JOIN FETCH el `CandidateProfile` en la JPQL de `findBySkillAndMinScore`, o cargar todos los profiles de los userIds resultado en una sola query con `findAllByUserIdIn(Set<Long>)`.

---

### M-3: N+1 query en SkillTestService.listActiveTests

**Archivo:** `src/main/java/com/surstudio/cts/assessment/application/SkillTestService.java:94-98`

**Descripción:**  
Por cada test activo, `toCandidateView` llama:
```java
attemptRepository.existsByUserIdAndSkillTestIdAndStatus(user.getId(), test.getId(), AttemptStatus.SUBMITTED)
```
Con 20 tests activos = 21 queries totales.

**Fix:** Una query que devuelva el `Set<Long>` de testIds ya completados por el usuario:
```java
Set<Long> completedTestIds = attemptRepository
    .findTestIdsByUserIdAndStatus(userId, AttemptStatus.SUBMITTED);
```
Y reemplazar la llamada por `completedTestIds.contains(test.getId())`.

---

### M-4: No se valida que la opción pertenece a la pregunta en OnboardingService

**Archivo:** `src/main/java/com/surstudio/cts/onboarding/application/OnboardingService.java:52-62`

**Descripción:**  
Se verifica que `questionId` existe y que `answerId` existe, pero no se verifica que la opción pertenece a la pregunta. En `AttemptService.submitAnswer` (línea 89) sí se hace esta validación. La omisión aquí permite guardar combinaciones incoherentes (opción de categoría "experiencia" guardada como respuesta a pregunta de "idioma").

**Fix:**
```java
if (!option.getQuestion().getId().equals(question.getId())) {
    throw new IllegalArgumentException(
        "Option " + entry.answerId() + " does not belong to question " + entry.questionId());
}
```

---

### M-5: durationMinutes sin validación de rango mínimo

**Archivo:** `src/main/java/com/surstudio/cts/assessment/dto/SkillTestRequest.java:11`

**Descripción:**  
`Integer durationMinutes` no tiene `@Min(1)`. Un ADMIN puede crear un test con `durationMinutes: 0` o `durationMinutes: -5`. Con la corrección de C-1, esto generaría un deadline en el pasado o exactamente en `now()`, haciendo que todos los attempts expiren instantáneamente al crearlos.

**Fix:**
```java
@Min(1) @Max(480) Integer durationMinutes
```

---

### M-6: UploadController usa ResponseStatusException en lugar de ProblemDetail

**Archivo:** `src/main/java/com/surstudio/cts/common/upload/UploadController.java:29-56`

**Descripción:**  
Toda la API retorna errores como `ProblemDetail` (RFC 7807) via `GlobalExceptionHandler`. `UploadController` usa `ResponseStatusException` directamente, produciendo un body distinto (`{ "status", "error", "message", "path" }` de Spring Boot).

Adicionalmente, la detección de extensión es frágil (`contentType.contains("png")` en lugar de un `switch` sobre MIME types completos o una librería), y no hay `@PreAuthorize` declarado (cualquier rol puede subir archivos, aunque esto puede ser intencional).

**Fix:** Lanzar `IllegalArgumentException` o una excepción custom que GlobalExceptionHandler ya maneja. Para la extensión, usar un `Map.of("image/png", ".png", "image/jpeg", ".jpg", "image/webp", ".webp")`.

---

### M-7: JwtAuthFilter silencia TODAS las excepciones

**Archivo:** `src/main/java/com/surstudio/cts/common/security/JwtAuthFilter.java:49-51`

**Descripción:**
```java
} catch (Exception ignored) {
    // Invalid token — proceed unauthenticated
}
```
Captura toda `Exception`, incluyendo `NullPointerException`, `OutOfMemoryError` (a través de `Error`... no, pero sí RuntimeExceptions inesperadas). Bugs en `JwtService` o `UserDetailsService` quedarían silenciados: el request se procesaría como anónimo en lugar de fallar con 500, haciendo que errores de configuración sean difíciles de detectar.

**Fix:**
```java
} catch (io.jsonwebtoken.JwtException | UsernameNotFoundException e) {
    // Invalid/expired token — proceed unauthenticated
    log.debug("JWT validation failed: {}", e.getMessage());
} catch (Exception e) {
    log.error("Unexpected error in JwtAuthFilter", e);
    // Re-throw or let proceed unauthenticated based on policy
}
```

---

### M-8: Credencial de admin documentada en plaintext en migración

**Archivo:** `src/main/resources/db/migration/V9__seed_admin_user.sql:1`

**Descripción:**
```sql
-- Seed: usuario admin para desarrollo. Password: admin123
```
El comentario documenta explícitamente la contraseña del usuario admin. Si este archivo llega a un repositorio público o a un ambiente no-dev sin rotación de credenciales, existe una cuenta ADMIN con contraseña conocida.

**Impacto:** Si el script se ejecuta en producción sin cambiar la contraseña, cualquier persona con acceso al repositorio tiene credenciales de admin.

**Fix:** Eliminar el comentario con la contraseña. Documentar en CLAUDE.md o en el README de deployment que el primer paso post-deploy es cambiar la contraseña del admin seed. Alternativamente, generar la migración con un hash de contraseña aleatoria y exponer la contraseña solo como variable de entorno al momento del seed.

---

### M-9: getTestForAdmin hereda transacción de lectura-escritura innecesariamente

**Archivo:** `src/main/java/com/surstudio/cts/assessment/application/SkillTestService.java:81-83`

**Descripción:**  
`SkillTestService` tiene `@Transactional` a nivel de clase (lectura-escritura). `listActiveTests` y `getTestForCandidate` tienen override correcto con `@Transactional(readOnly = true)`. Pero `getTestForAdmin` no tiene override: hereda la transacción de lectura-escritura, lo que deshabilita optimizaciones que Hibernate y el driver JDBC aplican en transacciones de solo lectura (flush innecesario al cierre, ausencia de dirty checking skip).

**Fix:** Agregar `@Transactional(readOnly = true)` a `getTestForAdmin`.

---

### M-10: Tests no cubren la compuerta de un-intento ni el deadline

**Archivos:** `src/test/java/com/surstudio/cts/attempt/application/AttemptServiceTest.java` y `CandidateFlowIntegrationTest.java`

**Descripción:**  
`AttemptServiceTest` no tiene ningún test que verifique que `startAttempt` lanza excepción cuando ya existe un attempt `IN_PROGRESS` para el mismo test. El test de `startAttempt_throwsWhenTestInactive` (línea 63) está bien, pero falta:
- `startAttempt_throwsWhenAttemptInProgress` — verifica one-attempt gate para IN_PROGRESS
- `submitAnswer_throwsWhenDeadlineExpired` — verificaría el fix de C-1
- `submitAttempt_throwsWhenDeadlineExpired`

`CandidateFlowIntegrationTest` no tiene un test que intente el mismo test dos veces. El test de IDOR (línea 204) verifica que candidato B no puede acceder al attempt de candidato A, pero no verifica que candidato A no pueda iniciar un segundo attempt.

**Fix:** Agregar los tests mencionados una vez implementados los fixes C-1 y C-2.

---

## Hallazgos Menores

### m-1: PatchUserRequest sin Bean Validation

**Archivo:** `src/main/java/com/surstudio/cts/identity/dto/PatchUserRequest.java`

`fullName`, `phone` y `avatarUrl` no tienen restricciones de longitud. Cualquier string de longitud arbitraria se acepta y persiste. El campo `fullName` en la entidad tiene `length = 100` (solo genera DDL si `ddl-auto != validate`), pero la columna en BD no tiene CHECK de longitud. Agregar `@Size(max = 100)` y `@Size(max = 30)` sobre los campos correspondientes.

---

### m-2: actuator/** completamente público

**Archivo:** `src/main/java/com/surstudio/cts/common/security/SecurityConfig.java:44`

`.requestMatchers("/actuator/**").permitAll()` expone todos los actuator endpoints sin auth. El yml limita la exposición a `health` e `info`, lo cual es razonable para load balancers. Sin embargo, si en el futuro se expone `metrics`, `env` o `loggers`, estarán públicos por defecto. Preferible: `.requestMatchers("/actuator/health").permitAll()` y requerir auth para el resto.

---

### m-3: Skill enum desincronizado del CLAUDE.md

**Archivo:** `src/main/java/com/surstudio/cts/assessment/domain/Skill.java:18-19` y `CLAUDE.md`

V11 agregó `METODOLOGIA` y `GESTION` al CHECK constraint en BD y al enum Java, pero CLAUDE.md en la sección "Skill enum" aún lista solo los 7 originales. Inconsistencia documental menor que puede confundir a futuros desarrolladores.

---

### m-4: Answer sin campo updatedAt

**Archivo:** `src/main/java/com/surstudio/cts/attempt/domain/Answer.java`

Cuando un candidato cambia su respuesta (upsert en `submitAnswer`), `created_at` no refleja la última modificación. No hay `updated_at`. Para auditoría y debugging es valioso saber cuándo fue modificada la respuesta (especialmente si se implementa tracking de cambios pre-submit). Agregar `@UpdateTimestamp private Instant updatedAt`.

---

### m-5: Detección de extensión de archivo por contenido MIME frágil

**Archivo:** `src/main/java/com/surstudio/cts/common/upload/UploadController.java:41-43`

```java
String ext = contentType.contains("png") ? ".png"
        : contentType.contains("webp") ? ".webp"
        : ".jpg";
```
`"image/png-extra"` matchearía como `.png`. Un content-type desconocido como `"image/gif"` cae en `.jpg`. Usar una estructura de lookup explícita:
```java
Map<String, String> EXTENSIONS = Map.of(
    "image/png", ".png", "image/jpeg", ".jpg", "image/webp", ".webp");
String ext = EXTENSIONS.getOrDefault(contentType, null);
if (ext == null) throw new ResponseStatusException(UNSUPPORTED_MEDIA_TYPE, ...);
```

---

### m-6: SkillTestController expone vista de candidato a ADMIN/RECRUITER sin documentación

**Archivo:** `src/main/java/com/surstudio/cts/assessment/api/SkillTestController.java:79-88`

`GET /api/v1/tests` y `GET /api/v1/tests/{id}` usan `@PreAuthorize("isAuthenticated()")`. Un RECRUITER accediendo a `/api/v1/tests/{id}` recibe la vista saneada (sin isCorrect) — esto es seguro, pero puede ser confuso ya que la ruta no es intuitiva para un recruiter. Consideración para versiones futuras.

---

## Lo Que Está Bien Hecho

**Trust boundary central es sólido:**  
`SkillTestCandidateView.OptionDto` (línea 17) no tiene campo `isCorrect` — es una garantía estructural, no solo de runtime. El test `candidateView_optionDto_hasNoCorrectComponent` (SkillTestServiceTest:127) verifica esto en tiempo de compilación. Excelente.

**Score 100% server-side:**  
`AttemptService.submitAttempt` calcula el score directamente desde `option.isCorrect()` en el servidor. El cliente no manda ni influye en el puntaje. Ningún DTO de request acepta `score` o `correct`.

**IDOR correctamente bloqueado en attempts:**  
`requireOwnership` (AttemptService:140-143) lanza `ResourceNotFoundException` (no `ForbiddenException`) al detectar acceso a un attempt ajeno — correctamente oscurece la existencia del recurso. El test de integración `idor_candidateBCannotAccessCandidateAAttempt` lo verifica end-to-end con Testcontainers.

**DTOs bien separados en el borde:**  
Ninguna entidad JPA se expone directamente en respuestas HTTP. Todos los controllers retornan records de DTO.

**Flyway ownership del esquema:**  
`spring.jpa.hibernate.ddl-auto: validate` correcto. Todas las migraciones están versionadas. La restricción `chk_skill_enum` en BD tiene coherencia con el enum Java (después de V11).

**Arquitectura de scoring con Strategy:**  
`ScoringStrategy` como interfaz + `FlatScoringStrategy` inyectado como `@Component` es un DIP correcto. Cambiar la estrategia de scoring no requiere modificar `AttemptService`.

**GlobalExceptionHandler completo y consistente:**  
Maneja `ResourceNotFoundException`, `ConflictException`, `IllegalArgumentException`, `MethodArgumentNotValidException`, `HttpMessageNotReadableException` y `MethodArgumentTypeMismatchException` — todos con `ProblemDetail` RFC 7807.

**Validaciones en endpoints de escritura:**  
`@Valid` en todos los `@RequestBody` de controllers. `SubmitAnswerRequest` valida `@NotNull`. `QuestionRequest` valida `@NotBlank` y `@NotEmpty` en options. `RegisterRequest` valida email y tamaño de contraseña.

**Tests de integración E2E con Testcontainers:**  
`CandidateFlowIntegrationTest` valida el flujo completo con PostgreSQL real, incluyendo verificación de que la response de candidato no contiene la palabra "correct". `AbstractIntegrationTest` con Testcontainers es un patrón robusto.

**Security config correctamente stateless:**  
JWT, `SessionCreationPolicy.STATELESS`, sin CSRF (apropiado para API REST). `@EnableMethodSecurity` habilita `@PreAuthorize` a nivel de método.

**Batch fetch size configurado:**  
`default_batch_fetch_size: 16` en application.yml mitiga N+1 para lazy loading de colecciones (questions, options) dentro de una misma transacción.

---

## Tabla de Priorización

| ID | Severidad | Ubicación principal | Descripción breve |
|----|-----------|--------------------|--------------------|
| C-1 | **CRÍTICO** | `AttemptService:146` | Deadline no se valida — anti-cheat desactivado |
| C-2 | **CRÍTICO** | `AttemptService:57` | One-attempt gate no bloquea IN_PROGRESS |
| C-3 | **CRÍTICO** | `UsersController:13,22,28` | @Transactional + repo en controller |
| C-4 | **CRÍTICO** | `GeneralInfoController:29` | POST sin @PreAuthorize de rol |
| M-1 | Moderado | `ProfileService:56` | N+1 en getSkillHistory |
| M-2 | Moderado | `RecruiterService:38` | N+1 en searchCandidates |
| M-3 | Moderado | `SkillTestService:134` | N+1 en listActiveTests |
| M-4 | Moderado | `OnboardingService:55` | Opción no validada vs. pregunta |
| M-5 | Moderado | `SkillTestRequest:11` | durationMinutes sin @Min(1) |
| M-6 | Moderado | `UploadController:29` | ResponseStatusException inconsistente |
| M-7 | Moderado | `JwtAuthFilter:49` | catch(Exception) silencia todo |
| M-8 | Moderado | `V9__seed_admin_user.sql:1` | Password admin documentada en plaintext |
| M-9 | Moderado | `SkillTestService:81` | getTestForAdmin con tx read-write |
| M-10 | Moderado | Tests | Falta coverage de one-attempt gate y deadline |
| m-1 | Menor | `PatchUserRequest.java` | Sin Bean Validation |
| m-2 | Menor | `SecurityConfig:44` | /actuator/** público |
| m-3 | Menor | `CLAUDE.md` | Skill enum desactualizado |
| m-4 | Menor | `Answer.java` | Sin updatedAt |
| m-5 | Menor | `UploadController:41` | Detección MIME frágil |
| m-6 | Menor | `SkillTestController:79` | Vista candidato accesible a todos los roles |
