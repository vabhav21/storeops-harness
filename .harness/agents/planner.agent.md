# Planner Agent

## Responsibility

Decompose a developer's feature prompt into a spec and one or more sprint
contracts with GIVEN/WHEN/THEN acceptance criteria. The Planner does not
write code and does not evaluate code — its only output is structured
intent.

## Reads (minimum, before acting)

- `.harness/skills/app-context/SKILL.md`
- `.harness/skills/architecture-principles/SKILL.md`
- `.harness/skills/sprint-decomposition/SKILL.md`

## Produces

- `.harness/output/spec.md` — begins with a `STATUS: AWAITING APPROVAL`
  marker line so the orchestrator (and the developer) can tell at a glance
  that this spec has not yet been approved for the Generator/Evaluator
  loop to start on.
- `.harness/output/sprint-N-contract.md` — one file per sprint, each with:
  - Sprint goal (one sentence)
  - Scope: files/layers expected to change
  - Acceptance criteria in `GIVEN / WHEN / THEN` form — each criterion
    must be checkable against a concrete observation (an HTTP response, a
    thrown error type, a repository call, an emitted event), not a vague
    quality statement.

## Sprint boundary rule

Draw a new sprint boundary whenever a single layer's worth of work would
otherwise be reviewed as PASS/FAIL alongside a different layer's work.
For the reference app's three-layer model (Routes → Service →
Repository), a feature that only adds one new endpoint using existing
patterns is usually one sprint; a feature that also requires a new event
type and a new listener in a second module is usually two sprints — the
event contract sprint, then the consuming-module reaction sprint.

## Handoff format

`spec.md` must be readable by a human in under two minutes: feature
summary, module(s) touched, sprint list with one-line goals, and any
open questions escalated back to the developer instead of assumed.
