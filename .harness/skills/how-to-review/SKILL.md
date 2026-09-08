# Skill: how-to-review

**Purpose:** Tell the Evaluator how to turn the Generator's output into a
verdict — the review procedure, independent of the specific pass/fail
thresholds (those live in `grading-criteria/SKILL.md`).

## Procedure

1. **Run automated checks first.** `mvn checkstyle:check spotbugs:check
   test`. Capture the exit code and the failing rule IDs / test names
   verbatim — do not paraphrase a Checkstyle violation, quote its rule
   ID.
2. **Check module boundaries mechanically.** Grep new/changed files for
   `import com.cognizant.storeops.<other-module>.repository` — any hit
   outside the module owning that repository is an automatic hard-gate
   FAIL on Rule 1 (module boundary).
3. **Check the error contract mechanically.** Grep new/changed `service`
   and `routes` files for `throw new RuntimeException` or `throw new
   Error` — any hit is an automatic hard-gate FAIL on Rule 3.
4. **Check event-bus usage by reading, not grepping.** For any new
   cross-module side effect described in `generator-summary.md`, open the
   relevant service method and confirm it calls `eventBus.emit(...)`
   rather than importing another module's service/repository for a
   write. Cite the file and line you inspected.
5. **Check layer separation by reading the routes and repository
   files changed.** Routes files should contain no `if` statements
   implementing business rules — only input validation and delegation.
   Repository files should contain no `@RestController`-style logic.
6. **Cross-check the AC self-check table** in `generator-summary.md`
   against the actual sprint contract — every acceptance criterion must
   have a corresponding test, and that test must actually assert the
   criterion's THEN clause, not merely exist.
7. **Compute the weighted score** per `grading-criteria/SKILL.md`, apply
   any hard gates, and write the verdict.

## Ambiguity fallback

If a check genuinely cannot be resolved from the available evidence
(e.g. the Generator's summary references a file that was not actually
changed), the Evaluator must **not** guess a pass. Mark that check FAIL
with the reason "insufficient evidence — <specifics>" and let the
Generator's next iteration supply clearer evidence, or let it trigger
escalation if iterations are exhausted.
