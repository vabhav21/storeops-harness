# StoreOps Harness — AI-Native Tech Architect Capstone (Build Track)

Working Claude Code development harness for the StoreOps retail store
operations REST API, built for Cognizant's AI-Native Tech Architect
programme, Capstone Case Study 1.

**Stack:** Java 17, Spring Boot 3.3, Spring Data JPA, H2 (in-memory),
JUnit 5 + MockMvc, Checkstyle + SpotBugs.

## Where to start as a reviewer

| Looking for... | Go to |
|---|---|
| The orchestration brain | `CLAUDE.md` |
| Agent definitions | `.harness/agents/*.agent.md` |
| Skill files (governance rules) | `.harness/skills/*/SKILL.md` |
| The demonstration feature prompt | `PROMPT.md` |
| Planner output for that feature | `.harness/output/spec.md`, `.harness/output/sprint-1-contract.md` |
| Generator → Evaluator → Monitor chain of evidence | `.harness/reviews/sprint-1-*.md` |
| Architectural reasoning | `DESIGN_BRIEF.md` |
| Honest self-assessment | `REFLECTION.md` |
| Deployment steps + verification | `DEPLOYMENT.md` |
| Informal build log (bonus) | `JOURNAL.md` |
| Application code | `src/main/java/com/cognizant/storeops/` |

## Running locally

```bash
mvn verify                                 # the Evaluator's hard gates + tests
mvn spring-boot:run                        # starts on http://localhost:8080
```

`checkstyle:check` and `spotbugs:check` are bound to the `verify` phase
(see `pom.xml`), so `mvn verify` is the reliable form of the Evaluator's
hard-gate command: SpotBugs analyses bytecode, and invoking it as a bare
goal before `compile` finds no classes to analyse. Configuration lives in
`checkstyle.xml` and `spotbugs-exclude.xml`. Requires JDK 17+.

Or via Docker — see `DEPLOYMENT.md`.

## Module map

| Module | Owns | Base path |
|---|---|---|
| `activities` | `Task` lifecycle, emits `TASK_COMPLETED` | `/api/activities` |
| `programmes` | `Project` + membership, templates | `/api/programmes` |
| `staff` | `User`, roles, store assignment | `/api/staff` |
| `alerts` | `Notification` delivery + read state | `/api/alerts` |
| `reports` | read-only cross-module aggregation | `/api/reports` |
| `shared` | `AppError` hierarchy, `EventBus`, handler | — |

See `.harness/skills/app-context/SKILL.md` for the full responsibility
table and `.harness/skills/api-integration/SKILL.md` for the endpoint and
event-contract inventory. The demonstration run targets `programmes`
(`POST /api/programmes/{id}/templates`).
