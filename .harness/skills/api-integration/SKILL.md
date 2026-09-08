# Skill: api-integration

**Purpose:** Tell the Generator exactly how new endpoints and cross-module
event contracts should be shaped for StoreOps, so integration surfaces
stay consistent sprint over sprint.

## REST conventions

- Base path: `/api/<module>` (plural module name from Section 3.3, e.g.
  `/api/programmes`, `/api/activities`).
- Resource creation: `POST /api/<module>` → `201 Created` with the full
  resource in the body.
- Resource lookup: `GET /api/<module>/{id}` → `200 OK`, or an `AppError`
  → `404` via `NotFoundError`.
- Sub-resource actions on an existing resource use a verb noun path
  segment: `POST /api/programmes/{id}/members`,
  `POST /api/programmes/{id}/close`,
  `POST /api/programmes/{id}/templates`.
- An action that triggers async/cross-module work but does not return
  the finished result synchronously returns `202 Accepted`, not `200` —
  e.g. `applyTemplate`, because the actual Task rows are created by the
  activities module's event listener, not synchronously within this
  request.

## Current endpoint inventory

Consult this before adding an endpoint — if a route already covers the
need, extend it rather than adding a near-duplicate path.

| Module | Method + path | Success | Notes |
|---|---|---|---|
| programmes | `POST /api/programmes` | 201 | create programme |
| programmes | `GET /api/programmes/{id}` | 200 | |
| programmes | `GET /api/programmes?storeId=` or `?regionId=` | 200 | one filter required |
| programmes | `POST /api/programmes/{id}/members` | 201 | |
| programmes | `POST /api/programmes/{id}/close` | 200 | emits `PROGRAMME_CLOSED` |
| programmes | `POST /api/programmes/{id}/templates` | 202 | emits `PROGRAMME_TEMPLATE_APPLIED` |
| activities | `POST /api/activities` | 201 | |
| activities | `GET /api/activities/{id}` | 200 | |
| activities | `PATCH /api/activities/{id}/status` | 200 | emits `TASK_COMPLETED` on DONE |
| staff | `POST /api/staff` | 201 | |
| staff | `GET /api/staff/{id}` | 200 | |
| alerts | `GET /api/alerts?staffId=` | 200 | |
| alerts | `POST /api/alerts/{id}/read` | 200 | |
| reports | `GET /api/reports/store/{storeId}` | 200 | read-only (Rule 5) |

Two absences are deliberate and must stay that way:

- **No `POST /api/alerts`.** Notifications are raised only in response to
  events. Exposing a create route would let callers bypass the event bus,
  which is Rule 2.
- **No write verbs under `/api/reports`.** Rule 5.

## Error responses

Every `AppError` maps to this body shape via `GlobalExceptionHandler`:

```json
{ "code": "VALIDATION_ERROR", "message": "...", "timestamp": "..." }
```

Do not hand-roll a different error body shape in any controller.

## Event contract: PROGRAMME_TEMPLATE_APPLIED

Emitted by `ProjectServiceImpl.applyTemplate(...)`. Payload:
`ProgrammeTemplateAppliedEvent(projectId, storeId, templateName,
List<TemplateTaskSpec>)`. A listener in the `activities` module (out of
scope for this sprint, tracked as a follow-up) is expected to create one
`Task` per `TemplateTaskSpec`, with `category = PLANOGRAM` and priority
taken from the spec.

## Event contract: PROGRAMME_CLOSED

Emitted by `ProjectServiceImpl.closeProject(...)`. Payload: the closed
programme's `UUID`. A listener in `reports` is expected to generate a
`STORE_SUMMARY` report.

## Event contract: TASK_COMPLETED

Emitted by `TaskServiceImpl.updateStatus(...)` when, and only when, a task
transitions into `DONE`. Payload: `TaskCompletedEvent(taskId, storeId,
projectId, category)`.

This is the seam that keeps the activities module from writing into
`alerts` or `reports`. When an SLA notification or a report refresh is
needed on completion, the consuming module subscribes to
`TASK_COMPLETED` — `TaskServiceImpl` must never inject
`NotificationService`, `NotificationRepository`, or `ReportRepository`.
