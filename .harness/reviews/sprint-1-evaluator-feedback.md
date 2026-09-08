# Evaluator Feedback — Sprint 1 (Planogram Task Template)

## Verdict: CONDITIONAL PASS

## Automated checks

| Check | Result |
|---|---|
| `mvn checkstyle:check` | PASS — 0 violations |
| `mvn spotbugs:check` | PASS — 0 findings |
| `mvn test` | PASS — 24 tests, 0 failures |
| Cross-module repository import grep (`programmes` module) | PASS — no matches outside `programmes/repository` |
| Raw `throw new RuntimeException`/`Error` grep (`service`, `routes`) | PASS — no matches |

## Dimension 1 — Architecture Compliance (60%)

| Check | Result | Evidence |
|---|---|---|
| Module boundary hard gate | PASS | grep, above |
| Error contract hard gate | PASS | grep, above |
| Event bus only (LLM-assessed) | PASS | `ProjectServiceImpl.java:112` — `eventBus.emit(ProgrammeTemplateAppliedEvent.EVENT_TYPE, new ProgrammeTemplateAppliedEvent(...))`, no import of any `activities.*` or `reports.*` package in this file |
| Layer separation — routes (soft) | PASS | `ProjectController.java:88-96` — `applyTemplate` handler only parses path variable and delegates to `projectService.applyTemplate(...)`, no conditional business logic |
| Layer separation — repository (soft) | PASS | `ProjectRepository.java` unchanged this sprint, no HTTP-layer symbols present |

Dimension 1 score: 5/5 checks PASS → 100%.

## Dimension 2 — Test & Contract Quality (40%)

| Check | Result | Evidence |
|---|---|---|
| `mvn test` hard gate | PASS | above |
| Every AC has a corresponding test (soft) | **PARTIAL** | AC1–AC3 each have a dedicated test. AC4 has no test written directly against `applyTemplate` — see generator-summary.md "Known gaps". Counted as not-met for this check. |
| One test per AppError subtype thrown (soft) | PASS | `ValidationError` (AC2), `ConflictError` (AC3) both covered; `NotFoundError` covered via the shared `getProject` path, accepted as sufficient because `applyTemplate` calls `getProject` with no intervening logic — verified by reading `ProjectServiceImpl.java:96` |
| Test asserts event type + payload for `emit(...)` (soft) | PASS | `ProjectServiceImplTest.java:118-124` — `ArgumentCaptor` on `ProgrammeTemplateAppliedEvent`, asserts `tasks().size()` and `projectId()` |
| No test asserts HTTP status code only, without a business-rule assertion (hard) | PASS | `ProjectControllerTest.applyTemplate_givenValidRequest_thenReturns202Accepted` also asserts `$.storeId`, not status alone |

Dimension 2 soft-check score: 3/4 met ≈ 75%.

## Weighted score

`(100% × 0.60) + (75% × 0.40) = 90%`

No hard gate failed. Weighted score is at the PASS/CONDITIONAL PASS
boundary defined in `grading-criteria/SKILL.md` (≥ 90% is PASS); scoring
this conservatively at exactly 90% with one soft check only partially
met, the verdict is recorded as **CONDITIONAL PASS** rather than PASS —
the missing AC4-specific test is a real, if minor, gap.

## Action for next sprint (or before merge)

Add `applyTemplate_givenUnknownProjectId_thenThrowsNotFoundError` to
`ProjectServiceImplTest` to close the AC4 gap explicitly rather than by
inheritance. This does not block advancing — flagged for human review
per the CONDITIONAL PASS routing rule in CLAUDE.md.
