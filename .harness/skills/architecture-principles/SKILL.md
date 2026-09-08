# Skill: architecture-principles

**Purpose:** Encode the five non-negotiable StoreOps architecture rules
(Section 3.5 of the capstone spec) as concrete, checkable statements —
not generic "write clean code" advice. Each rule maps to one of the four
failure modes the client's standards team observed in a prior AI-assisted
experiment.

## Rule 1 — Module boundary

**Prohibits:** Any module importing directly from another module's
`repository` package.

**StoreOps-specific example:**
`ProjectServiceImpl` (programmes) must never import
`com.cognizant.storeops.staff.repository.StaffRepository`. If it needs
staff data, it calls `StaffService` (a service-layer, read-only lookup).

**What breaks without it:** the client's standards team's failure mode
#1 — direct repository imports across module boundaries — reappears, and
a change to the staff module's schema silently breaks programmes at
compile time or, worse, at runtime via stale assumptions about the
staff table's columns.

## Rule 2 — Event bus only

**Prohibits:** A module directly calling another module's service to
cause a *write* (a side effect), instead of emitting an event.

**StoreOps-specific example:** `ProjectServiceImpl.applyTemplate(...)`
must call `eventBus.emit("PROGRAMME_TEMPLATE_APPLIED", payload)` and must
never import `activities.service.TaskService` to create Task rows
directly. Likewise `closeProject(...)` emits `PROGRAMME_CLOSED` rather
than calling into `reports.service.ReportService`.

**What breaks without it:** failure mode #4 — state changes written
directly to sibling module repositories — and the reports module's
"read-only" guarantee (Rule 5) becomes unenforceable because nothing
stops any module writing anywhere.

## Rule 3 — Error contract

**Prohibits:** `throw new RuntimeException(...)` or any raw `Error` in a
`service` or `routes` class.

**StoreOps-specific example:** `ProjectServiceImpl` throws
`ValidationError`, `NotFoundError`, or `ConflictError` — all subclasses
of `AppError` — never a bare exception. `GlobalExceptionHandler` is the
only place an `AppError` is translated into an HTTP response.

**What breaks without it:** failure mode #2. API consumers get
inconsistent error shapes and the client's standards team cannot build a
single error-handling convention on top of the API.

## Rule 4 — Layer separation

**Prohibits:** Business logic in `routes`; HTTP or cross-module logic in
`repository`.

**StoreOps-specific example:** `ProjectController` only binds HTTP,
validates the `role` enum string, and delegates to `ProjectService`. All
business rules (e.g. "cannot add a member to a CLOSED programme") live in
`ProjectServiceImpl`, never in `ProjectController`.

## Rule 5 — Read-only reports

**Prohibits:** The `reports` module writing to `activities`, `programmes`,
or `staff`.

**StoreOps-specific example:** a `ReportService` may call
`ProjectService.listByRegion(...)` to aggregate, but must never call
anything that persists a `Project` or `ProjectMember`.
