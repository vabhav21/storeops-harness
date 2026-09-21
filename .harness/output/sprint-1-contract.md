# Sprint 1 Contract — Planogram Task Template (emitting side)

**Derived from:** `.harness/output/spec.md`, "Sprint list" → Sprint 1.
This contract covers the emitting side only; spec.md's "Module(s)
touched" section places the `activities` listener explicitly out of
scope, and spec.md's open question (whether re-applying a template
should be rejected) is resolved here as *always allowed*, matching the
Planner's stated default.

**Boundary rule this contract inherits:** `PROMPT.md`'s requirement that
`programmes` must not write into the `activities` module's tables — this
is what makes AC1 assert an emitted event rather than created Task rows.

## Goal

`programmes` module exposes `POST /api/programmes/{id}/templates` and
emits `PROGRAMME_TEMPLATE_APPLIED` with the correct payload when called
on an ACTIVE programme with a non-empty task list.

## Scope

- `programmes/dto`: `TemplateTaskSpec`, `ApplyTemplateRequest`
- `programmes/service`: `ProjectService.applyTemplate`,
  `ProjectServiceImpl.applyTemplate`, `ProgrammeTemplateAppliedEvent`
- `programmes/routes`: `ProjectController.applyTemplate`
- Tests: `ProjectServiceImplTest`, `ProjectControllerTest` (template
  cases)

## Acceptance criteria

### AC1

```
GIVEN an ACTIVE Project with id {id}
WHEN POST /api/programmes/{id}/templates is called with a non-empty
     list of TemplateTaskSpec
THEN the response is 202 Accepted, and EventBus.emit is called exactly
     once with event type "PROGRAMME_TEMPLATE_APPLIED" and a payload
     whose `tasks` field has the same size as the request's task list
```

### AC2

```
GIVEN an ACTIVE Project with id {id}
WHEN applyTemplate is called with an empty task list
THEN a ValidationError is thrown and EventBus.emit is never called
```

### AC3

```
GIVEN a CLOSED Project with id {id}
WHEN applyTemplate is called with any non-empty task list
THEN a ConflictError is thrown and EventBus.emit is never called
```

### AC4

```
GIVEN a Project id that does not exist
WHEN applyTemplate is called
THEN a NotFoundError is thrown
```
