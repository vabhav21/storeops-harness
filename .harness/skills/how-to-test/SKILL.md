# Skill: how-to-test

**Purpose:** Prevent failure mode #3 from the client context — tests that
assert HTTP status codes but not business rule compliance — by giving the
Generator a concrete test pattern to follow for every new service method
and every new endpoint.

## Test structure: GIVEN/WHEN/THEN, one behaviour per test

Every service-layer test method name states the given/when/then in the
method name and asserts a **business outcome**, not just "no exception
was thrown":

```java
@Test
void closeProject_givenAlreadyClosedProgramme_thenThrowsConflictError() { ... }

@Test
void closeProject_givenActiveProgramme_thenEmitsProgrammeClosedEvent() { ... }
```

A test that only asserts `assertDoesNotThrow(...)` or only asserts an
HTTP status code, with no check on the resulting entity state or emitted
event, does not satisfy this skill and will be flagged by the Evaluator's
`grading-criteria` checklist.

## Two layers of tests, two different concerns

- **Service tests** (`*ServiceImplTest`, plain JUnit 5 + Mockito, no
  Spring context): assert business rules — the right `AppError` subtype
  for the right precondition, the right event type and payload emitted,
  the right resulting entity state. Mock `ProjectRepository` and
  `EventBus`.
- **Controller tests** (`*ControllerTest`, `@WebMvcTest` + MockMvc, mock
  the service): assert the HTTP contract — status code, response body
  shape, error body shape for `AppError`. Do NOT re-test business rules
  here; the service is mocked, so there is nothing to re-test.

## Coverage expectation for this repository

Every new public method on a `*Service` interface needs at minimum:

1. One test for the happy path.
2. One test per distinct `AppError` the method can throw.
3. One test asserting any `EventBus.emit(...)` call, with the exact
   event type string and a captured payload assertion — not just
   "emit was called".

## Automated check command (Java stack)

`mvn checkstyle:check spotbugs:check test` — the Evaluator runs this
verbatim as a hard gate. `BUILD SUCCESS` with 0 Checkstyle/SpotBugs
violations and all tests passing is required for PASS.
