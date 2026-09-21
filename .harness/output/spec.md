STATUS: AWAITING APPROVAL

# Spec — Planogram Task Template

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
