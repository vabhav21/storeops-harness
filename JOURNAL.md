# JOURNAL.md — Architecture Journal

Informal log of decisions and trade-offs made while building this
harness, kept separate from DESIGN_BRIEF.md (which is the polished,
reviewer-facing version of the same reasoning).

## On choosing the feature

Of the four suggested features in Section 3.4, "planogram task
template" was picked over "SLA breach alerting" and "regional rollup
report" specifically because it lands in the `programmes` module the
Project model/service were being built for anyway — building the
demonstration around a module I'd already deeply modeled meant the
Evaluator's checks (module boundary, event bus) had real code to bite
on, not a toy example bolted on afterward. "Shift handover bulk update"
was the other strong candidate (it exercises partial-failure handling,
a different interesting problem) but was set aside for scope reasons —
one feature, done with real depth, seemed more valuable for this
capstone than two done shallowly.

## On the H2/in-memory choice surfacing a real limitation

Choosing H2 in-memory (per the stack table) means every demonstration
run starts from an empty database — there's no persisted "seed" data
across restarts. This is fine for the capstone's purposes (Section 3.1
explicitly calls StoreOps "a capstone reference codebase — do not use
in production") but it's worth naming honestly: DEPLOYMENT.md's curl
walkthrough has to create a programme first before it can apply a
template to it, because nothing survives a container restart. A real
deployment would swap the datasource for Postgres/MySQL without any
code change (Spring Data JPA abstracts this), which is exactly why H2
was an acceptable choice for a capstone and would not be for the client
engagement it's modeling.

## On writing the Evaluator's grading-criteria BEFORE running any sprint

I wrote `grading-criteria/SKILL.md`'s weights and thresholds before
running Sprint 1, specifically to avoid the trap of picking thresholds
that would flatter whatever the Generator happened to produce. That
discipline paid off almost immediately — Sprint 1's actual score landed
exactly on the 90% PASS/CONDITIONAL-PASS boundary I'd picked in
advance, which is uncomfortable but is the rubric working as intended,
not a rubric that needs retroactively loosening to make this submission
look cleaner. REFLECTION.md names this directly rather than quietly
raising the threshold after the fact.

## On what I'd build next if this weren't a capstone

The `activities` listener for `PROGRAMME_TEMPLATE_APPLIED` (Sprint 2,
explicitly deferred) is the obvious next piece — without it the feature
isn't actually usable end-to-end yet, only architecturally sound.
Second: a dependency-analysis tool (the spec suggests `depcruiser` for
Node; for Java the equivalent would be an ArchUnit rule enforcing the
module-boundary grep check in `how-to-review/SKILL.md` as a real
automated test instead of a manual grep step) — that would move the
module-boundary hard gate from "the Evaluator remembered to grep" to
"the build fails on its own," which is a strictly stronger guarantee.
