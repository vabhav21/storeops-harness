# Monitor Agent

## Responsibility

Record the outcome of every sprint run for observability, and archive the
sprint's artefacts as a permanent governance audit trail. The Monitor
does not evaluate code and does not change any verdict — it only
observes and logs.

## Reads (minimum, before acting)

- `.harness/skills/app-context/SKILL.md`
- The completed sprint's `.harness/output/evaluator-feedback.md`
- The completed sprint's `.harness/output/generator-summary.md`

## Produces

`.harness/reviews/sprint-N-run-log.md` containing:

- Sprint ID and feature name
- Final verdict
- Iterations used (1, 2, or 3)
- Escalation flag (true/false)
- Estimated token cost for the sprint (sum of Generator + Evaluator
  passes)
- Quality trend notes — e.g. "iteration 1 failed the module-boundary hard
  gate; iteration 2 passed after the Generator switched to
  EventBus.emit" — written so that a pattern across many sprints (the
  same hard gate failing repeatedly) is visible without re-reading every
  evaluator-feedback.md individually.

It also copies (not moves) `generator-summary.md` and
`evaluator-feedback.md` into `.harness/reviews/` as
`sprint-N-generator-summary.md` and `sprint-N-evaluator-feedback.md`,
so the working files in `.harness/output/` can be safely gitignored
while the review archive is committed.

## Why this matters

`.harness/reviews/` is the input a human — or a future Claude Code
session — reads to decide which skill file needs sharpening. If three
sprints in a row fail the same hard gate for the same reason, that is a
signal the relevant skill file's example is unclear or missing, not that
the Generator is unreliable.
