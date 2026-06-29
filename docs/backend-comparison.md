# Comparación de Backends — Express/Supabase vs Spring Boot

Referencia técnica para entender las diferencias de arquitectura, contrato y decisiones de diseño entre los dos backends del Coach Talent System.

---

## Stack

| Aspecto | Express / Supabase (TFI) | Spring Boot (CTS API) |
|---|---|---|
| Runtime | Node.js 20 | JVM 21 (Java 21) |
| Framework | Express 4 | Spring Boot 3.3 |
| Lenguaje | TypeScript strict | Java 21 |
| ORM | Sequelize 6 | Spring Data JPA (Hibernate) |
| Esquema DB | Sequelize migrations (JS) | Flyway (SQL versionado) |
| Auth | Supabase Auth (JWT externo) | JWT propio (JJWT 0.12) |
| Storage | Supabase Storage | Filesystem local (`/uploads`) |
| Logger | Pino | Spring Boot default (Logback) |
| Package manager | Yarn | Maven Wrapper |
| Puerto default | 4000 | 8080 |

---

## Envelope de respuesta HTTP

Esta es la diferencia más importante para cualquier cliente.

### Express/Supabase — siempre un wrapper

```json
// Éxito
{ "result": true, "data": { ... }, "errorCode": 0, "message": null }

// Error
{ "result": false, "data": null, "errorCode": 3001, "message": "Descripción" }
```

Todos los endpoints — incluyendo errores — devuelven este objeto. El cliente **siempre** parsea `body.data`.

### Spring Boot — recurso directo + RFC 7807

```json
// Éxito → el recurso directamente
{ "id": 1, "email": "...", "role": "candidate" }

// Error → ProblemDetail (RFC 7807)
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid skill 'X'. Valid values: acrobacia, artistica, ..."
}
```

El cliente lee el body directamente en éxito, y usa `response.status` + `detail` en error.

---

## Autenticación

| Característica | Express/Supabase | Spring Boot |
|---|---|---|
| Proveedor JWT | Supabase Auth | Propio (JJWT) |
| Refresh token | Sí — devuelto en login/register | No — token único con TTL configurable |
| Token expirado | `errorCode: 3000` → cliente refresca | HTTP 401 → cliente vuelve a loguear |
| Endpoint refresh | `/api/auth/refresh` | No existe |
| Validación de token | Supabase verifica | `JwtAuthFilter` lo valida localmente |
| Role en token | Claim `role` | Claim `role` |

**Implicación práctica:** Un cliente que integre el Spring backend no necesita lógica de refresh. Un JWT bien configurado (ej. 24h) cubre una sesión completa.

---

## URL base y versionado

| | Express/Supabase | Spring Boot |
|---|---|---|
| Base URL | `http://localhost:4000` | `http://localhost:8080` |
| Prefijo de rutas | `/api/` | `/api/v1/` |
| Versionado | Ninguno | `v1` en el path |

---

## Endpoints comparados

### Autenticación

| Acción | Express/Supabase | Spring Boot |
|---|---|---|
| Registro | `POST /api/auth` | `POST /api/v1/auth/register` |
| Login | `POST /api/auth/login` | `POST /api/v1/auth/login` |
| Refresh | `POST /api/auth/refresh` | ❌ No existe |

### Usuario

| Acción | Express/Supabase | Spring Boot |
|---|---|---|
| Obtener perfil | `GET /api/users/me` | `GET /api/v1/users/me` |
| Actualizar perfil | `PATCH /api/users/me` | `PATCH /api/v1/users/me` |
| Cambiar contraseña | `PATCH /api/users/me/password` | ❌ No existe |
| Subir avatar | `PUT /api/users/me/avatar` | `POST /api/v1/upload` (multipart) |
| Ver perfil candidato | incluido en `GET /api/users/me` | `GET /api/v1/users/me/profile` |
| Editar perfil candidato | `PATCH /api/users/me` | `PUT /api/v1/users/me/profile` |

### Tests y evaluaciones

| Acción | Express/Supabase | Spring Boot |
|---|---|---|
| Listar tests activos | `GET /api/questions/test` | `GET /api/v1/tests` |
| Ver test por ID | no existe separado | `GET /api/v1/tests/{id}` |
| Crear test (admin) | no existe | `POST /api/v1/tests` |
| Editar test (admin) | no existe | `PUT /api/v1/tests/{id}` |
| Agregar pregunta (admin) | no existe | `POST /api/v1/tests/{id}/questions` |
| Iniciar intento | `POST /api/answer/start` | `POST /api/v1/tests/{id}/attempts` |
| Responder pregunta | `POST /api/answer` | `POST /api/v1/attempts/{id}/answers` |
| Enviar y obtener score | `POST /api/answer` (parcial) | `POST /api/v1/attempts/{id}/submit` |
| Historial de scores | no existe | `GET /api/v1/users/me/skill-history` |

### General Information

| Acción | Express/Supabase | Spring Boot |
|---|---|---|
| Obtener categorías + opciones | `GET /api/general_information` | `GET /api/v1/general-information` |
| Guardar respuestas | `POST /api/general_information` | `POST /api/v1/general-information` |

### Research Wizard (onboarding preguntas por tipo)

| Acción | Express/Supabase | Spring Boot |
|---|---|---|
| Listar preguntas por tipo | `GET /api/questions?type=ONBOARDING_CAN` | ❌ Removido (Speculative Generality) |
| Guardar respuestas | `POST /api/answer/onboarding` | ❌ Removido |
| Secciones disponibles | `GET /api/sections` | ❌ Removido |

### Recruiter

| Acción | Express/Supabase | Spring Boot |
|---|---|---|
| Buscar candidatos | mocked localStorage | `GET /api/v1/recruiter/candidates?skill=X&minScore=Y` |
| Rankings | mocked localStorage | ❌ No existe |
| Mensajería | mocked localStorage | ❌ No existe |

---

## Scoring — diferencia de diseño crítica

### Express/Supabase — client-side (inseguro)
El frontend calcula el score en `services/test-session.ts`. El servidor recibe el resultado ya calculado. Un candidato puede manipular su score interceptando la request.

### Spring Boot — server-side (trust boundary)
El servidor es la única autoridad del score. El campo `isCorrect` nunca sale del backend. El cliente nunca manda un puntaje. El score se calcula en `FlatScoringStrategy` al momento de `POST /attempts/{id}/submit`.

```
score = respuestas_correctas / total_preguntas  (0–100%)
```

Esta es una diferencia de diseño no negociable en el Spring backend.

---

## Manejo de errores

| Aspecto | Express/Supabase | Spring Boot |
|---|---|---|
| Formato | `{ result: false, errorCode: N, message: "..." }` | ProblemDetail RFC 7807 |
| Código de error de negocio | Sí (`errorCode` numérico) | No — solo status HTTP + `detail` |
| Token expirado | `errorCode: 3000` | HTTP 401 |
| Recurso no encontrado | `errorCode: N` | HTTP 404 + `detail` |
| Validación | `errorCode: N` + `message` | HTTP 400 + `detail` |

---

## Gestión del esquema de base de datos

| Aspecto | Express/Supabase | Spring Boot |
|---|---|---|
| Herramienta | Sequelize migrations (JS) | Flyway (SQL puro) |
| Formato | `YYYYMMDDNNNNNN-nombre.js` | `VN__descripcion.sql` |
| DDL automático | No (`sync: false`) | No (`ddl-auto: validate`) |
| Schema owner | Sequelize migrations | Flyway — única fuente de verdad |

---

## Arquitectura de paquetes

### Express/Supabase — por capa
```
src/
├── controllers/   # reciben HTTP, delegan a services
├── services/      # lógica de negocio
├── models/        # Sequelize models
├── routes/        # definición de rutas
├── middlewares/   # auth, roles
└── lib/           # utilidades
```

### Spring Boot — por feature + DIP
```
com.surstudio.cts/
├── identity/      # auth, users, perfiles
│   ├── api/       # controllers
│   ├── application/ # services
│   ├── domain/    # entidades + repos (interfaces)
│   └── dto/
├── assessment/    # banco de preguntas
├── attempt/       # rendir test, scoring, historial
├── onboarding/    # general information
├── recruiter/     # búsqueda de candidatos
└── common/        # seguridad, excepciones, upload
```

El dominio define interfaces (puertos); la infraestructura las implementa (adapters). Spring inyecta.

---

## Lo que tiene cada backend que el otro no tiene

### Solo en Express/Supabase
- Refresh token (`POST /api/auth/refresh`)
- Research Wizard (preguntas por tipo `ONBOARDING_CAN` / `ONBOARDING_REC`)
- Supabase Storage para archivos (URL pública CDN)
- Cambio de contraseña (`PATCH /api/users/me/password`)
- Health check (`GET /api/health`)

### Solo en Spring Boot
- Scoring server-side verificado (trust boundary)
- Banco de preguntas editable por admin (CRUD completo)
- Intento de test con estado (`STARTED` → `SUBMITTED`)
- Historial de scores por skill (`GET /api/v1/users/me/skill-history`)
- Búsqueda real de candidatos por skill y score mínimo
- Skill como enum tipado con validación estricta
- Tests completos (77 — unitarios, slice, integración con Testcontainers)
- Documentación automática Swagger (`/swagger-ui.html`)
- Actuator (`/actuator/health`)
