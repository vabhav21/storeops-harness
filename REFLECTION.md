# REFLECTION.md

## What the harness did well

The Evaluator caught a real, specific gap rather than rubber-stamping
the sprint: AC4 (not-found handling) had no dedicated test, and the
Evaluator's grep-plus-read procedure surfaced that as a named, cited
finding (`ProjectServiceImpl.java:96`) rather than a vague "coverage
could be better." That is exactly the behaviour the client's standards
team asked for in Section 2 — feedback specific enough to act on without
a human re-reviewing the diff. The module-boundary and event-bus checks
also worked as designed: because `ProjectServiceImpl.applyTemplate`
genuinely uses `EventBus.emit(...)` instead of importing another
module's service, the hard gates passed cleanly on the first iteration,
and the Evaluator's citation (file + line) meant I didn't have to take
its word for it — I could verify the claim in about ten seconds.

## Where it fell short

The CONDITIONAL PASS routing exposed a soft edge in `grading-criteria`:
scoring the missing-AC4-test check as "not met" produced exactly 90%,
which sits on the PASS/CONDITIONAL-PASS boundary I'd defined as "≥90%
is PASS." I resolved the tie conservatively toward CONDITIONAL PASS,
but a rubric that produces boundary-line ambiguity on its very first
real run is a rubric that needs a tie-breaking rule, not just a
threshold. Separately, AC4's "inherited coverage" reasoning
(`applyTemplate` calls `getProject` first with no intervening logic, so
`getProject`'s existing not-found test covers it) is a judgment call the
Evaluator made once, correctly, but nothing in `how-to-test/SKILL.md`
tells it — or a future Generator — that inherited coverage is
acceptable at all. It worked by the Evaluator reading carefully, not by
the skill file being unambiguous.

## One concrete improvement I would make

Add an explicit rule to `how-to-test/SKILL.md`: "A precondition check
inherited unchanged from a method this one calls (e.g. `getProject`'s
not-found check) may rely on that method's existing test, but the
Generator must say so explicitly in `generator-summary.md`'s Known Gaps
section, and the Evaluator must verify the call site has genuinely
added no new logic before accepting it as covered." That converts a
judgment call the Evaluator currently makes ad hoc into a checkable,
repeatable rule — closing exactly the kind of soft-check ambiguity that
produced this sprint's boundary-line CONDITIONAL PASS.
