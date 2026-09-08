# ARCHITECTURE.md — StoreOps API Structure and Request Flow

Diagrams derived from the code in `src/main/java/com/cognizant/storeops/`,
not from the design docs. Where the code and the docs disagree, the code
wins and the difference is called out under [Notes](#notes-on-what-the-diagrams-show).

**Stack:** Java 17 · Spring Boot 3.3.2 · Spring Data JPA · H2 (in-memory)
· JUnit 5 + MockMvc · Checkstyle + SpotBugs

---

## 1. Architecture diagram

Five domain modules, each with three layers, plus a `shared` package that
carries the error hierarchy and the event bus. Solid arrows are
compile-time dependencies that exist today; dashed arrows are event
subscriptions that are specified but **not yet implemented**.

```mermaid
flowchart TB
    Client(["HTTP Client"])

    subgraph SH["shared — cross-cutting, no domain logic"]
        direction TB
        AE["AppError · abstract<br>code · message · statusCode"]
        SUBS["NotFoundError → 404 NOT_FOUND<br>ValidationError → 400 VALIDATION_ERROR<br>ConflictError → 409 CONFLICT"]
        GEH["GlobalExceptionHandler<br>@RestControllerAdvice<br>AppError → JSON error body"]
        EB["EventBus · interface<br>InMemoryEventBus<br>wraps ApplicationEventPublisher"]
        SUBS -->|extends| AE
        AE -->|caught by| GEH
    end

    subgraph PRG["programmes"]
        direction TB
        PC["ProjectController<br>/api/programmes"]
        PS["ProjectServiceImpl"]
        PR[("ProjectRepository")]
        PC --> PS
        PS --> PR
    end

    subgraph ACT["activities"]
        direction TB
        AC["TaskController<br>/api/activities"]
        AS["TaskServiceImpl"]
        AR[("TaskRepository")]
        AC --> AS
        AS --> AR
    end

    subgraph STF["staff — read-only to other modules"]
        direction TB
        SC["StaffController<br>/api/staff"]
        SS["UserServiceImpl"]
        SR[("UserRepository")]
        SC --> SS
        SS --> SR
    end

    subgraph ALR["alerts — no inbound module dependencies"]
        direction TB
        LC["AlertController<br>/api/alerts"]
        LS["NotificationServiceImpl"]
        LR[("NotificationRepository")]
        LC --> LS
        LS --> LR
    end

    subgraph RPT["reports — read-only aggregation"]
        direction TB
        RC["ReportController<br>/api/reports"]
        RS["ReportServiceImpl"]
        RR[("ReportRepository")]
        RC --> RS
        RS --> RR
    end

    Client --> PC
    Client --> AC
    Client --> SC
    Client --> LC
    Client --> RC

    RS -->|"read-only, service layer only"| PS
    RS -->|"read-only, service layer only"| AS

    PS ==>|"emit PROGRAMME_TEMPLATE_APPLIED<br>emit PROGRAMME_CLOSED"| EB
    AS ==>|"emit TASK_COMPLETED"| EB

    EB -.->|"planned: create PLANOGRAM Tasks"| AS
    EB -.->|"planned: raise SLA notification"| LS
    EB -.->|"planned: refresh STORE_SUMMARY"| RS

    H2[("H2 in-memory<br>jdbc:h2:mem:storeops")]
    PR --> H2
    AR --> H2
    SR --> H2
    LR --> H2
    RR --> H2
```

### Boundary rules, and how the diagram shows them upheld

| Rule | Evidence in the diagram |
|---|---|
| No cross-module repository access | Every arrow into a cylinder starts inside the same module subgraph |
| No circular imports | The only inter-module solid edges are `reports → programmes` and `reports → activities`; nothing points back into `reports` |
| Cross-module side effects via event bus only | `programmes` and `activities` reach other modules only through the thick `EventBus` edges |
| `staff` read-only to other modules | No module has an edge into `staff` at all — it is a leaf |
| `reports` writes nothing outside itself | `reports` calls only reading methods on `ProjectService` / `TaskService` |

---

## 2. Flow diagram

`POST /api/programmes/{id}/templates` — the harness demonstration
feature, and the one endpoint that exercises validation, conflict
handling, typed errors, and the event bus in a single request.

```mermaid
sequenceDiagram
    autonumber
    actor C as HTTP Client
    participant Ctl as ProjectController<br>routes
    participant Svc as ProjectServiceImpl<br>service
    participant Repo as ProjectRepository<br>repository
    participant Bus as InMemoryEventBus<br>shared
    participant Pub as ApplicationEventPublisher
    participant Eh as GlobalExceptionHandler

    C->>Ctl: POST /api/programmes/:id/templates<br>ApplyTemplateRequest
    Ctl->>Svc: applyTemplate(id, request)

    Note over Ctl: routes layer does HTTP binding only,<br>no business rules

    Svc->>Repo: findById(id)
    Repo-->>Svc: Optional of Project

    alt programme does not exist
        Svc-)Eh: throw NotFoundError
        Eh-->>C: 404 · code NOT_FOUND
    else programme status is CLOSED
        Svc-)Eh: throw ConflictError
        Eh-->>C: 409 · code CONFLICT
    else request task list null or empty
        Svc-)Eh: throw ValidationError
        Eh-->>C: 400 · code VALIDATION_ERROR
    else all preconditions hold
        Svc->>Bus: emit PROGRAMME_TEMPLATE_APPLIED,<br>ProgrammeTemplateAppliedEvent
        Bus->>Pub: publishEvent StoreOpsEvent
        Note over Pub: no subscriber yet — the activities<br>listener is a tracked follow-up
        Pub-->>Bus: returns
        Bus-->>Svc: returns
        Svc-->>Ctl: Project · unmodified
        Ctl-->>C: 202 Accepted · ProjectResponse
    end
```

### Why `202`, and why the response carries no tasks

`programmes` never creates `Task` rows — that is the `activities`
module's data. It emits the intent and returns `202 Accepted`, so the
caller knows the work was admitted but is not yet complete. Returning
`201` with a task list would require `ProjectServiceImpl` to import
`activities`, which is exactly the coupling the module boundary exists
to prevent. See `DESIGN_BRIEF.md` Section D, Decision 1.

### Error path shape

All three error branches converge on one handler, so every failure —
from any endpoint in any module — returns the same body:

```json
{ "code": "CONFLICT", "message": "...", "timestamp": "2026-09-08T..." }
```

No controller builds an error `ResponseEntity` itself, and no service or
route throws a raw `RuntimeException`.

---

## Notes on what the diagrams show

1. **The three events have no subscribers yet.** `PROGRAMME_CLOSED`,
   `PROGRAMME_TEMPLATE_APPLIED`, and `TASK_COMPLETED` are all emitted;
   `grep -rn "@EventListener" src/main/java` returns nothing. The dashed
   arrows are the contracted-but-unbuilt half. This is deliberate — the
   consuming side was split into a follow-up sprint (`DESIGN_BRIEF.md`
   Section A) — but it means `alerts` is currently reachable only via its
   own HTTP routes.

2. **`reports` reads its sibling modules synchronously,** through
   `ProjectService` and `TaskService`. That is a real compile-time
   dependency, permitted because it targets the service layer (not
   repositories) and calls no mutating method.

3. **Persistence is JPA over embedded H2,** not plain in-memory
   collections. No external database is needed, but the repositories are
   `JpaRepository` interfaces.

4. **`GlobalExceptionHandler` is drawn as a participant receiving throws.**
   In reality the exception propagates up the call stack through the
   controller and is intercepted by Spring's `@RestControllerAdvice`; the
   diagram compresses that into a direct edge for readability.

---

## Regenerating / verifying

```bash
JAVA_HOME="C:/Users/240422/jdk-17.0.11" mvn -B clean verify
```

Both diagrams were checked against the source at commit `3f0b374`
(35 tests, Checkstyle and SpotBugs clean).
