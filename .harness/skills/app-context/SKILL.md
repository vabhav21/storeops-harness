# Skill: app-context

**Purpose:** Give every agent the same baseline understanding of what
StoreOps is and how it is organised, so the Planner, Generator, Evaluator,
and Monitor never disagree about domain vocabulary.

## What StoreOps is

StoreOps is a retail store operations management REST API. It lets store
teams run operational programmes (seasonal rollouts, compliance drives,
store refits), track operational activities (restocking, planogram
resets, audits) against those programmes, coordinate staff, receive
in-app alerts, and pull performance reports.

## Modules and their one-sentence responsibility

| Module | Responsibility |
|---|---|
| `activities` | Operational activities — restocking, planogram resets, compliance checks, general tasks |
| `programmes` | Store programmes and their staff membership |
| `staff` | Staff registration, auth, profile |
| `alerts` | In-app/email notifications triggered by operational events |
| `reports` | Read-only aggregation across activities, programmes, staff |

## Stack (this repository)

Java 17, Spring Boot 3.3, Spring Data JPA, H2 (in-memory), JUnit 5 +
MockMvc, Checkstyle + SpotBugs. Build/verify command: `mvn checkstyle:check
spotbugs:check test`.

## Base URL and running locally

`mvn spring-boot:run` starts the app on `http://localhost:8080`.
H2 console: `http://localhost:8080/h2-console` (JDBC URL
`jdbc:h2:mem:storeops`).

## Where things live

- `src/main/java/com/cognizant/storeops/<module>/routes` — HTTP layer
- `src/main/java/com/cognizant/storeops/<module>/service` — business logic
- `src/main/java/com/cognizant/storeops/<module>/repository` — data access
- `src/main/java/com/cognizant/storeops/<module>/model` — JPA entities
- `src/main/java/com/cognizant/storeops/<module>/dto` — request/response DTOs
- `src/main/java/com/cognizant/storeops/shared/error` — AppError hierarchy
- `src/main/java/com/cognizant/storeops/shared/events` — EventBus
