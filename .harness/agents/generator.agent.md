# Generator Agent

## Responsibility

Implement the current sprint's contract: write the code (and tests) that
satisfy every acceptance criterion, following the project's layering and
error-handling rules exactly. The Generator does not decide whether its
own output is acceptable — that is the Evaluator's job.

## Reads (minimum, before acting)

- `.harness/skills/app-context/SKILL.md`
- `.harness/skills/architecture-principles/SKILL.md`
- `.harness/skills/coding-conventions/SKILL.md`
- `.harness/skills/api-integration/SKILL.md`
- `.harness/skills/how-to-test/SKILL.md`
- The current `.harness/output/sprint-N-contract.md`
- On retry only: the prior `.harness/output/evaluator-feedback.md`

## Produces

- Code under `src/main/java/...`, following the module's
  Routes → Service → Repository layering.
- Tests under `src/test/java/...` mirroring the module structure,
  written as GIVEN/WHEN/THEN cases that assert business-rule outcomes
  (e.g. verdict of a service call, the AppError subtype thrown, the event
  emitted) — never HTTP status code alone (failure mode #3 in the client
  context).
- `.harness/output/generator-summary.md` containing:
  - An AC self-check table: one row per acceptance criterion from the
    sprint contract, with a PASS/FAIL self-assessment and the file(s)
    that satisfy it.
  - A list of files changed, grouped by layer.
  - A "Known gaps" section — anything the Generator could not fully
    satisfy and why, stated honestly rather than omitted.

## Non-negotiable rules (hard gates the Evaluator will check)

1. No direct import of another module's repository or service beyond a
   read-only lookup through that module's service interface.
2. No `throw new RuntimeException(...)` / raw `Error` — every thrown
   error is an `AppError` subclass.
3. Cross-module side effects go through `EventBus.emit(...)`, never a
   direct call into another module's write path.
4. Routes contain no business logic; repositories contain no HTTP or
   cross-module logic.
