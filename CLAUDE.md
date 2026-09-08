# CLAUDE.md — StoreOps Harness Orchestrator

This file is read automatically by Claude Code when launched in this
repository. It defines how the Planner, Generator, Evaluator, and Monitor
agents are invoked and how control passes between them.

## 1. Entry prompt format

A developer starts a harness run with a single prompt directed at the Planner:

```
@planner <feature description>
```

Example (used for this repository's demonstration run — see PROMPT.md):

```
@planner Add planogram task template — POST /api/programmes/{id}/templates
to clone a standard set of PLANOGRAM tasks into a new store programme,
applying department assignments and default priorities from the template
definition.
```

## 2. Agent files

| Agent | File | Invoked by |
|---|---|---|
| Planner | `.harness/agents/planner.agent.md` | Developer, via `@planner` prompt |
| Generator | `.harness/agents/generator.agent.md` | Orchestrator, once per sprint |
| Evaluator | `.harness/agents/evaluator.agent.md` | Orchestrator, after each Generator pass |
| Monitor | `.harness/agents/monitor.agent.md` | Orchestrator, after every sprint verdict |

## 3. Orchestration sequence

1. Developer invokes `@planner <feature>`.
2. Planner reads `app-context`, `architecture-principles`, and
   `sprint-decomposition` skills, then writes `.harness/output/spec.md`
   with a `STATUS: AWAITING APPROVAL` marker at the top, plus one
   `.harness/output/sprint-N-contract.md` per sprint.
3. Orchestrator halts and prints the spec summary. **The developer's only
   required action here is to review spec.md and type `APPROVED`.**
4. On `APPROVED`, the orchestrator starts the Generator/Evaluator loop for
   sprint 1 — this loop runs autonomously; the developer does not manually
   trigger each iteration.

## 4. Generator/Evaluator loop (per sprint)

```
sprint_iteration = 1
loop:
    Generator reads sprint-N-contract.md + its skills
        -> writes code under src/, tests under src/test/
        -> writes .harness/output/generator-summary.md

    Evaluator reads generator-summary.md + evaluation skills
        -> runs automated checks (mvn checkstyle:check spotbugs:check test)
        -> writes .harness/output/evaluator-feedback.md with verdict:
             PASS | CONDITIONAL PASS | FAIL

    Monitor reads evaluator-feedback.md + generator-summary.md
        -> writes .harness/reviews/sprint-N-run-log.md
        -> archives generator-summary.md and evaluator-feedback.md
           to .harness/reviews/ as sprint-N-generator-summary.md /
           sprint-N-evaluator-feedback.md

    if verdict == PASS:
        advance to sprint N+1 (or finish if last sprint)
    elif verdict == CONDITIONAL PASS:
        advance, but Monitor flags the sprint for later human review
    elif verdict == FAIL and sprint_iteration < 3:
        feed evaluator-feedback.md back to Generator as context
        sprint_iteration += 1
        retry
    else: # FAIL on 3rd iteration
        write .harness/output/escalation.md naming:
          - the sprint
          - iteration count (3)
          - the specific blocking issue(s) from evaluator-feedback.md
        halt and notify the developer
```

**Maximum iterations per sprint: 3.** This bound exists so an Evaluator
that keeps failing the same Generator output for a subtly different reason
each time cannot burn unbounded tokens — after 3 failed attempts the
problem is judged to need human judgement, not another AI pass.

## 5. Context scoping strategy

- Each agent invocation is a **fresh context window** — the Planner does
  not carry Generator/Evaluator conversation history, and vice versa.
  Agents only see what their "Reads" list in `.harness/agents/*.agent.md`
  specifies, plus the current sprint's handoff files.
- This prevents context window degradation across a long multi-sprint run:
  a Generator on sprint 4 does not carry stale reasoning from sprint 1's
  failed attempts, only sprint 4's contract and any retry feedback for the
  *current* sprint.
- Skill files are the mechanism for carrying durable project knowledge
  across resets — they are re-read fresh each time rather than relied on
  from memory.

## 6. CI/CD relationship

The Evaluator's automated checks (`mvn checkstyle:check spotbugs:check
test`) **precede** the existing CI pipeline — they are a local, fast-fail
gate the harness runs before code is ever pushed. They do not replace CI:
the same commands are expected to also run in the project's CI/CD
pipeline (`.github/workflows/`, kept separate from `.harness/` per
Section 2.2) as a second, independent check on `main`. If the harness
Evaluator ever passes something CI fails, that gap is a skill-file
correction to make, tracked via `.harness/reviews/`.
