# Generator Summary — Sprint 1 (Planogram Task Template)

## AC self-check

| AC | Self-assessment | Satisfied by |
|---|---|---|
| AC1 | PASS | `ProjectServiceImplTest.applyTemplate_givenActiveProgrammeAndTasks_thenEmitsEventNotDirectRepositoryWrite`, `ProjectControllerTest.applyTemplate_givenValidRequest_thenReturns202Accepted` |
| AC2 | PASS | `ProjectServiceImplTest.applyTemplate_givenEmptyTaskList_thenThrowsValidationError` |
| AC3 | PASS | `ProjectServiceImplTest.applyTemplate_givenClosedProgramme_thenThrowsConflictError` |
| AC4 | PASS (inherited) | Covered indirectly via `getProject` reuse — `ProjectServiceImplTest.getProject_givenUnknownId_thenThrowsNotFoundError` exercises the same `getProject` path `applyTemplate` calls first |

## Files changed

**Service layer**
- `programmes/service/ProjectService.java` — added `applyTemplate` to
  the interface
- `programmes/service/ProjectServiceImpl.java` — implemented
  `applyTemplate`, added `EventBus.emit` call
- `programmes/service/ProgrammeTemplateAppliedEvent.java` — new event
  payload record

**Routes layer**
- `programmes/routes/ProjectController.java` — added
  `POST /{id}/templates` handler

**DTOs**
- `programmes/dto/TemplateTaskSpec.java` — new
- `programmes/dto/ApplyTemplateRequest.java` — new

**Tests**
- `ProjectServiceImplTest.java` — 3 new test methods for AC1–AC3
- `ProjectControllerTest.java` — 1 new test method for AC1 (HTTP
  contract)

## Known gaps

- AC4 has no dedicated test written specifically against
  `applyTemplate` with an unknown id — it relies on `getProject`'s
  existing coverage since `applyTemplate` calls `getProject` first with
  no additional logic before that call. Flagged rather than hidden: the
  Evaluator should decide whether inherited coverage is sufficient or a
  dedicated test is required.
- The `activities` module listener that would actually create Task rows
  from `PROGRAMME_TEMPLATE_APPLIED` is not implemented — out of scope
  per spec.md, tracked as a follow-up sprint.
