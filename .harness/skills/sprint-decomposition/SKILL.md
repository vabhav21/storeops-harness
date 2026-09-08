# Skill: sprint-decomposition

**Purpose:** Give the Planner a repeatable method for turning a feature
prompt into sprint contracts with testable acceptance criteria.

## Method

1. Identify which module(s) the feature touches, using
   `app-context/SKILL.md`'s module table.
2. Identify whether the feature needs a **new cross-module event**. If
   yes, that is reason enough for a sprint boundary — sprint 1 defines
   the emitting side and its contract test (does the right event fire
   with the right payload); a following sprint (or a separately tracked
   follow-up, if the consuming module is out of scope this round) would
   implement the listener.
3. For each sprint, write acceptance criteria as:

   ```
   GIVEN <precondition on entity state or request>
   WHEN <the action under test — an HTTP call or service method call>
   THEN <an observable outcome: HTTP status + business-rule state,
         an AppError subtype, OR an emitted event + payload shape>
   ```

   A criterion that cannot be rewritten this way is not concrete enough
   yet — push back into the feature description for detail rather than
   inventing an assumption.

## Worked example — this repository's demonstration sprint

**Feature:** Add planogram task template — clone a standard set of
PLANOGRAM tasks into a store programme.

**Sprint 1 goal:** `programmes` module exposes
`POST /api/programmes/{id}/templates` and emits
`PROGRAMME_TEMPLATE_APPLIED` with the correct payload; it does not
implement the `activities` listener (tracked as a follow-up, out of
scope this sprint since the reference app's `activities` module is a
stub in this repository).

**Example acceptance criterion (verbatim, in full):**

```
GIVEN an ACTIVE Project with id {id}
WHEN POST /api/programmes/{id}/templates is called with a non-empty
     list of TemplateTaskSpec
THEN the response is 202 Accepted, and EventBus.emit is called exactly
     once with event type "PROGRAMME_TEMPLATE_APPLIED" and a payload
     whose `tasks` field has the same size as the request's task list
```

This is testable without ambiguity: the test mocks `EventBus`, calls the
service, and asserts both the HTTP status and the captured event
payload — satisfying `how-to-test/SKILL.md`'s rule against status-codeonly assertions.
