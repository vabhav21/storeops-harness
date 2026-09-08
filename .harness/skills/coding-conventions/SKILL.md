# Skill: coding-conventions

**Purpose:** Give the Generator the specific Java/Spring conventions this
codebase follows, so generated code is indistinguishable in style from
hand-written code in this repository.

## Naming

- Entities: singular noun, e.g. `Project`, `ProjectMember` — never
  `Projects` or `ProjectEntity`.
- Enums: singular noun + `Status`/`Role`/`Type` suffix matching Section
  3.3's key-types table exactly, e.g. `ProjectStatus`, `ProjectRole`,
  `TaskCategory`. Do not invent alternate names for these — they are the
  project's ubiquitous language.
- Service interfaces: `<Entity>Service`; implementations:
  `<Entity>ServiceImpl`.
- DTOs: `<Verb><Entity>Request` for inputs (`CreateProjectRequest`),
  `<Entity>Response` for outputs (`ProjectResponse`).

## Package layout (per module)

```
<module>/
  routes/       @RestController classes — HTTP binding + validation only
  service/      interface + *Impl — business logic, throws AppError
  repository/   Spring Data JpaRepository interfaces only
  model/        @Entity classes, enums
  dto/          request/response records
```

## Entities

- Use `UUID` primary keys with `@GeneratedValue(strategy =
  GenerationType.UUID)`.
- Protected no-arg constructor for JPA; a public constructor for the
  fields that must be set at creation time.
- Do not expose entity setters for fields that represent business
  invariants (e.g. `status`) without also encoding the transition rule in
  the service layer — the entity setter existing is not permission to
  skip the service-layer check.

## DTOs

- Prefer Java `record` for all request/response DTOs — they are
  immutable by default, which matches the "DTOs are not business
  objects" principle.
- Never return a JPA entity directly from a `routes` class — always map
  through a `<Entity>Response` record (see `ProjectResponse.from(...)`).

## Errors

- Never `throw new RuntimeException(...)`. Pick the closest existing
  `AppError` subclass (`ValidationError`, `NotFoundError`,
  `ConflictError`); add a new subclass under
  `shared/error/` only if none fits, and give it a clear HTTP status.

## Events

- Event type strings are `UPPER_SNAKE_CASE` verbs describing what
  happened, past tense: `PROGRAMME_CLOSED`, `PROGRAMME_TEMPLATE_APPLIED`.
- Event payloads are records, not entities — never emit a JPA entity on
  the bus (it may be lazily-loaded and fail to serialize outside the
  transaction).
