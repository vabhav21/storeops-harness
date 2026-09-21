STATUS: AWAITING APPROVAL

# Spec — Planogram Task Template

**Source prompt:** `PROMPT.md` — *"Add planogram task template — POST
/api/programmes/{id}/templates to clone a standard set of PLANOGRAM
tasks into a new store programme, applying department assignments and
default priorities from the template definition. The programmes module
must not write directly into the activities module's tables; the intent
to create tasks must be raised as a cross-module event."* Invoked as
`@planner <feature>` per `CLAUDE.md` Section 1.

The prompt's final sentence is what drives the event-bus decision in
Sprint 1's acceptance criteria below, and is traced through to
`DESIGN_BRIEF.md` Section D, Decision 1.

## Feature summary

Allow a store programme to have a standard set of PLANOGRAM tasks
cloned into it in one call, so store managers don't hand-create the same
recurring restocking/planogram checklist for every new programme.

## Module(s) touched

- `programmes` (primary — new endpoint, new event emission)
- `activities` (event consumer — **out of scope this sprint**; the
  reference `activities` module in this repository is currently a stub,
  so the listener is tracked as a follow-up rather than built against a
  stub that would need to be re-done)

## Sprint list

1. **Sprint 1** — `programmes` exposes
   `POST /api/programmes/{id}/templates`; `ProjectService.applyTemplate`
   validates the programme is not CLOSED and the task list is non-empty,
   then emits `PROGRAMME_TEMPLATE_APPLIED` on the event bus with the
   full task list as payload. Returns `202 Accepted`.

## Open questions (escalated, not assumed)

- Should re-applying the same template name to a programme be rejected
  as a duplicate, or always allowed? **Assumption made:** always allowed
  for this sprint — a programme may want more than one planogram pass
  (e.g. two seasonal resets). Flagged in DESIGN_BRIEF.md Section D as a
  decision the developer should confirm before this ships to the client.
