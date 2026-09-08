# Skill: grading-criteria

**Purpose:** The Evaluator's scoring rubric. Two dimensions, weights
summing to 100%, each with deterministic hard gates.

## Dimension 1 — Architecture Compliance (60%)

| Check | Type | Hard gate? |
|---|---|---|
| `mvn checkstyle:check` passes | Automated | **Yes** |
| `mvn spotbugs:check` passes | Automated | **Yes** |
| Zero cross-module repository imports | Automated (grep) | **Yes** |
| Zero raw `throw new RuntimeException`/`Error` in service/routes | Automated (grep) | **Yes** |
| Cross-module side effects use `EventBus.emit(...)` | LLM-assessed, cited | **Yes** |
| Routes contain no business logic | LLM-assessed, cited | No — soft check |
| Repositories contain no HTTP/cross-module logic | LLM-assessed, cited | No — soft check |

Any hard-gate failure in this dimension → verdict is **FAIL**
regardless of Dimension 2's score. These correspond directly to failure
modes #1, #2, and #4 in the client context — they are non-negotiable
because a single miss here is exactly what the standards team said they
will not accept.

## Dimension 2 — Test & Contract Quality (40%)

| Check | Type | Hard gate? |
|---|---|---|
| `mvn test` passes (BUILD SUCCESS) | Automated | **Yes** |
| Every AC in the sprint contract has a corresponding test | LLM-assessed, cited | No — soft check |
| At least one test per `AppError` subtype the method can throw | LLM-assessed, cited | No — soft check |
| At least one test asserts event type + payload for any `emit(...)` call | LLM-assessed, cited | No — soft check |
| No test asserts only an HTTP status code with no business-rule assertion | LLM-assessed, cited | **Yes** |

The last row directly targets failure mode #3 (tests that check status
codes but not business rules) and is a hard gate for that reason — a
soft check would let exactly the failure mode the client flagged slip
through with a "good enough" score.

## Verdict rules

- **FAIL**: any hard gate in either dimension fails.
- **PASS**: no hard gate fails, and the weighted soft-check score
  (Dimension 1 soft checks × 60% + Dimension 2 soft checks × 40%) is
  ≥ 90%.
- **CONDITIONAL PASS**: no hard gate fails, weighted soft-check score is
  ≥ 70% and < 90% — advance to the next sprint, but Monitor flags the
  sprint for human review in `run-log.md`.
- Anything below 70% with no hard-gate failure is treated as **FAIL**
  (not CONDITIONAL PASS) — a low soft score usually means the Generator
  misunderstood the contract, which another iteration should fix, rather
  than something to wave through.

## Worked example (verdict determinism)

Given: Checkstyle/SpotBugs/tests all pass; grep finds no cross-module
repository import; the Evaluator reads `applyTemplate` and confirms
`eventBus.emit("PROGRAMME_TEMPLATE_APPLIED", ...)` is used (cited at
`ProjectServiceImpl.java:112`); one AC lacks a dedicated test.
→ All hard gates pass. Soft-check score: 6/7 checks met ≈ 86%.
→ Verdict: **CONDITIONAL PASS**. Same inputs next time → same verdict,
by construction — nothing here depends on model temperature or phrasing.
