# DESIGN_BRIEF.md — Harness Design Brief

**Capstone:** AI-Native Tech Architect Programme — Case Study 1, Build Track
**Project:** StoreOps Retail Store Operations Management REST API
**Stack:** Java 17 / Spring Boot 3.3 / H2 (in-memory) / JUnit 5 + MockMvc / Checkstyle + SpotBugs

---

## Section A — Intent Decomposition

### How the feature was broken into sprint contracts

The chosen feature — "add a planogram task template" — touches two
modules: `programmes`, which owns the new endpoint, and `activities`,
which would eventually own the resulting `Task` rows. The sprint
boundary was drawn at that module seam, not arbitrarily by file count.
`sprint-decomposition/SKILL.md` states the rule directly: *if a feature
needs a new cross-module event, that is reason enough for a sprint
boundary.* Sprint 1 covers only the emitting side — the `programmes`
endpoint and the `EventBus.emit("PROGRAMME_TEMPLATE_APPLIED", ...)`
call — and stops there. A second sprint (out of scope for this
submission, and explicitly flagged as a follow-up in `spec.md`) would
implement the `activities` module's listener that actually creates
`Task` rows.

This boundary choice was deliberate for a second reason beyond the
event seam: the reference `activities` module in this repository is
currently a stub (per Section 2.3 of the capstone spec — StoreOps is
bootstrapped with stub implementations). Building a listener against a
stub that would need to be redone once `activities` gets real business
logic would produce throwaway work. Splitting here means Sprint 1's
output is durable regardless of what `activities` eventually looks
like — the event contract (`ProgrammeTemplateAppliedEvent`'s shape) is
the only thing the two sprints share, and event payloads are
deliberately designed to be stable, versioned contracts rather than
internal implementation detail.

### How acceptance criteria were structured

Every acceptance criterion follows `GIVEN <precondition on entity
state or request> / WHEN <the action under test> / THEN <an observable
outcome>`. The discipline that makes a criterion testable rather than
subjective is in the THEN clause: it must resolve to one of exactly
three observable things — an HTTP status code *combined with* a
business-rule assertion (never status code alone, per failure mode #3),
a specific `AppError` subtype, or an emitted event's type string plus a
concrete assertion on its payload. A criterion that can't be rewritten
into one of those three shapes was pushed back into the feature
description for more detail rather than assumed — this is why `spec.md`
carries an explicit "Open questions" section (whether re-applying a
template twice should be rejected) instead of silently picking an
answer.

### Example sprint contract entry, in full

```
### AC1

GIVEN an ACTIVE Project with id {id}
WHEN POST /api/programmes/{id}/templates is called with a non-empty
     list of TemplateTaskSpec
THEN the response is 202 Accepted, and EventBus.emit is called exactly
     once with event type "PROGRAMME_TEMPLATE_APPLIED" and a payload
     whose `tasks` field has the same size as the request's task list
```

This is unambiguous to both the Generator (it names the exact class —
`EventBus`, the exact string — `"PROGRAMME_TEMPLATE_APPLIED"`, and the
exact assertion — payload size equality) and the Evaluator (it can be
checked by reading one method and one test, with no interpretation
required about what "correct" means).

---

## Section B — Governance Framework

### Skill file strategy

Seven skill files split cleanly into three tiers, matching the minimum
structure required (shared foundation, ≥2 Generator-specific, ≥2
Evaluator-specific) with one extra for the Planner:

| File | Shared across | What it encodes that is StoreOps-specific |
|---|---|---|
| `app-context` | All agents | The five-module map, the actual stack (Java 17/Spring Boot 3.3/H2), the actual run/build commands |
| `architecture-principles` | All agents | The five numbered rules from Section 3.5, each with a StoreOps class name as the example (`ProjectServiceImpl`, `eventBus.emit("PROGRAMME_CLOSED", ...)`) — not a generic "respect module boundaries" statement |
| `sprint-decomposition` | Planner only | The specific rule that a new cross-module event is itself a sprint boundary, with this repository's own Sprint 1 as the worked example |
| `coding-conventions` | Generator | Exact naming rules tied to Section 3.3's entity/enum table (`ProjectStatus`, `ProjectRole` — not the Generator's own invented names) |
| `api-integration` | Generator | The two live event contracts this codebase actually has (`PROGRAMME_TEMPLATE_APPLIED`, `PROGRAMME_CLOSED`) with their exact payload shapes |
| `how-to-test` | Generator | The specific anti-pattern (status-code-only assertions) named in the client's own failure mode #3, with this repo's actual test class names as examples |
| `how-to-review` | Evaluator | A step-by-step procedure keyed to this repo's actual package names (`grep import com.cognizant.storeops.<module>.repository`) |
| `grading-criteria` | Evaluator | Weighted dimensions with a worked example from this repo's own Sprint 1 evaluator-feedback.md |

`app-context` and `architecture-principles` are shared across every
agent for a specific reason: they are the only two files where a
disagreement between agents would be catastrophic. If the Planner and
the Evaluator held different mental models of what the five modules do,
a spec could ask for something the Evaluator would then correctly
reject as an architecture violation — wasting a full sprint. Every
other skill file is scoped to the one agent that needs it, to keep each
agent's context window small and cheap (see Section C below on
cost-awareness).

### How `.harness/reviews/` functions as a governance audit trail

Every sprint leaves three files behind in `.harness/reviews/`:
`sprint-N-generator-summary.md`, `sprint-N-evaluator-feedback.md`, and
`sprint-N-run-log.md`. Together they form a chain of evidence — contract
→ generator output → evaluator verdict → observability record — that
answers, for any past sprint, "what was asked for, what was built, was
it accepted, and why." Any engineer on the eight-developer squad (not
just the person who ran the harness) can open this folder and audit a
decision without re-running anything. It would surface a recurring
quality issue the way `sprint-1-run-log.md`'s "Quality trend notes"
section is designed to be read: if three consecutive `run-log.md` files
all note the same skill-file gap (this repository's Sprint 1 already
flagged one — inherited not-found coverage isn't an explicit rule in
`how-to-test`), that repetition is the trigger to edit the skill file,
not to keep tolerating the same CONDITIONAL PASS every sprint.

### One skill file rule, stated and explained

**Rule (from `architecture-principles/SKILL.md`, Rule 2 — Event bus
only):** *"`ProjectServiceImpl.applyTemplate(...)` must call
`eventBus.emit("PROGRAMME_TEMPLATE_APPLIED", payload)` and must never
import `activities.service.TaskService` to create Task rows directly."*

**What breaks without it:** if the Generator instead autowired
`TaskService` directly into `ProjectServiceImpl`, the two modules would
become compile-time coupled — a schema or method-signature change in
`activities` would force a recompile of `programmes`, defeating the
whole point of drawing module boundaries in the first place. It also
reproduces failure mode #4 from the client context exactly:
"missing event bus integration — state changes written directly to
sibling module repositories." The event-bus indirection is what lets
Sprint 1 (emitting side) ship independently of Sprint 2 (consuming
side) ever being built — which is precisely the boundary this brief's
Section A used to justify the sprint split.

---

## Section C — Non-Determinism Strategy

### Evaluation dimensions, weights, and why

Two dimensions: **Architecture Compliance (60%)** and **Test & Contract
Quality (40%)**. The 60/40 split reflects that three of the four
failure modes named in the client context (#1 module boundary, #2
error contract, #4 event bus) are architecture concerns, while only one
(#3, weak tests) is a testing concern — the weighting is a direct,
traceable response to the client's own diagnosis of what went wrong
last time, not an arbitrary split.

### Hard gate conditions, and why each cannot be a soft check

| Hard gate | Failure mode it prevents | Why it cannot be soft |
|---|---|---|
| Zero cross-module repository imports | #1 | A "mostly respects boundaries" outcome still compiles a coupling into the codebase that the next change makes worse, not better — there is no acceptable partial credit for a compile-time dependency that shouldn't exist |
| Zero raw `RuntimeException`/`Error` in service/routes | #2 | API consumers build error-handling logic against the `AppError` contract; one un-typed throw breaks that contract for every caller, not just this endpoint |
| Cross-module side effects use `EventBus.emit` | #4 | Same reasoning as the module-boundary gate — a direct call is a coupling regardless of how "reasonable" it looks in isolation |
| No test asserts HTTP status code only | #3 | This is the client's named complaint verbatim; a soft check here would mean the Evaluator could pass exactly the anti-pattern the client asked to have eliminated, given a high enough score elsewhere |

### Verdict determinism, walked through one example

Sprint 1's actual run: automated checks (Checkstyle, SpotBugs, `mvn
test`) all passed; the module-boundary and error-contract greps found
zero matches; the Evaluator read `ProjectServiceImpl.java:112` and
confirmed `eventBus.emit(...)` was used, citing the exact line. All four
hard gates therefore passed — this alone rules out FAIL regardless of
anything else. Dimension 1's soft checks (layer separation for routes
and repository) both passed by inspection, so Dimension 1 scored 100%.
Dimension 2's hard gate (`mvn test` passing, and no status-code-only
test) both passed, but one soft check — "every AC has a corresponding
test" — was only partially met, because AC4 (not-found handling) had no
dedicated test of its own. That produced a Dimension 2 soft score of
75%. The weighted total, `(100% × 0.60) + (75% × 0.40) = 90%`, sits
exactly on this harness's PASS/CONDITIONAL-PASS boundary (`≥90% is
PASS`); given a real partial gap rather than a rounding artefact, the
Evaluator resolved the tie toward CONDITIONAL PASS. Re-running the
identical check results through the identical rubric produces the
identical 90% and the identical verdict every time — nothing here
depends on model phrasing or temperature, only on the check results
themselves, which is the property this section exists to guarantee.

### Escalation path

Escalation triggers when a sprint reaches a `FAIL` verdict on its third
Generator/Evaluator iteration (see `CLAUDE.md` Section 4). The
orchestrator writes `.harness/output/escalation.md` naming the sprint
number, the iteration count (always 3 at that point), and the specific
blocking issue(s) copied from that iteration's `evaluator-feedback.md`
hard-gate failures. It is received by the developer who invoked the
Planner — the orchestrator halts the loop entirely rather than retrying
again, on the reasoning laid out in `CLAUDE.md`: three failed attempts
at the same contract is evidence the problem needs human judgement
(an ambiguous contract, a genuinely hard architectural question), not
a fourth AI pass with the same skill files and the same likely outcome.

---

## Section D — Architectural Decisions

### Decision 1 — Cross-module task creation via event, not synchronous call

**Alternatives considered:** (a) `ProjectService` calls
`ActivitiesService.createTasks(...)` synchronously and returns `201`
with the created tasks in the response; (b) emit an event and return
`202 Accepted` without the created tasks.

**Rationale:** Option (a) reads more convenient for API consumers but
violates the event-bus-only rule (Section 3.5) and reintroduces failure
mode #4. Option (b) costs the API a slightly less satisfying response
(no task list back immediately) but keeps the module boundary intact
and matches how `PROGRAMME_CLOSED` already works in this codebase.

**Assumption this depends on:** API consumers can tolerate an
eventually-consistent result for this endpoint (i.e., they will poll or
otherwise check for the created tasks rather than requiring them in the
immediate response). If the client's actual UI needs the task list
synchronously, this decision would need revisiting — likely by having
the event listener respond on a different channel, not by reverting to
a direct call.

### Decision 2 — Splitting the emitting and consuming sides into separate sprints (or scopes) entirely

**Alternatives considered:** (a) build both sides in one sprint against
the current `activities` stub; (b) split as done — emitting side only,
consuming side as a tracked follow-up.

**Rationale:** Building against a stub `activities` module risks
building the wrong shape of listener, since the stub's eventual real
implementation isn't finalized. Splitting keeps Sprint 1's deliverable
durable and testable in isolation (verified via a mocked `EventBus`),
at the cost of the feature not being fully end-to-end functional yet.

**Assumption this depends on:** the event contract
(`ProgrammeTemplateAppliedEvent`'s field shape) will not need to change
once `activities` is built out for real. If it does change, Sprint 1's
tests (which assert the current payload shape) would need updating
alongside the listener sprint.

### Decision 3 — Allowing multiple template applications per programme

**Alternatives considered:** (a) reject a second `applyTemplate` call
on a programme that already had one applied, treating it as a
duplicate; (b) always allow it.

**Rationale:** Chose (b) because a programme legitimately might need
two planogram passes (e.g., a mid-season adjustment). Rejecting would
require tracking "has a template already been applied," which is state
this sprint doesn't otherwise need to persist.

**Assumption this decision depends on:** this was recorded as an open
question in `spec.md` rather than silently decided — the assumption is
that the developer reviewing `spec.md` before typing `APPROVED` would
either confirm or override it before the sprint ran. For this
submission's demonstration run, it was left as the Planner's stated
default.
