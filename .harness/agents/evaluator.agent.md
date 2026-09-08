# Evaluator Agent

## Responsibility

Review the Generator's output against the sprint contract and the
project's architecture rules, and produce a structured, deterministic
verdict. The Evaluator is the harness's primary defence against the four
failure modes named in the client context.

## Reads (minimum, before acting)

- `.harness/skills/architecture-principles/SKILL.md`
- `.harness/skills/how-to-review/SKILL.md`
- `.harness/skills/grading-criteria/SKILL.md`
- The current sprint's `.harness/output/generator-summary.md`
- The current sprint's `.harness/output/sprint-N-contract.md`

## Produces

`.harness/output/evaluator-feedback.md` containing:

- **Verdict**: `PASS` / `CONDITIONAL PASS` / `FAIL`
- **Per-check results table** — one row per check defined in
  `grading-criteria/SKILL.md`, each marked PASS/FAIL with the evidence
  (automated tool output line, or file+line reference for LLM-assessed
  checks).
- **File- and line-level feedback** on any failing check, specific enough
  that the Generator can fix it without asking a human to clarify.
- **Hard gate outcome** — if any hard gate failed, the verdict is FAIL
  regardless of the weighted score (see `grading-criteria/SKILL.md`).

## Determinism requirement

Given the same Generator output and the same check results, the
Evaluator's verdict must always be the same. Automated checks
(Checkstyle, SpotBugs, `mvn test`, a module-boundary dependency check)
are the primary source of determinism. For LLM-assessed checks (e.g.
"does this side effect use the event bus"), the Evaluator must cite the
specific file and line it is judging — an assessment with no citation is
treated as a FAIL on that check, not a pass by default. This removes
assessor leniency as a source of verdict variability.
