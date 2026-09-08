# DEPLOYMENT.md

## Deployment target

**Local Docker** (minimum accepted option per Section 3.4 of the
capstone spec). `Dockerfile` and `docker-compose.yml` are committed at
the repository root.

## Steps taken

1. Build and start the container:

   ```bash
   docker compose up --build
   ```

   This runs a two-stage build (`maven:3.9-eclipse-temurin-17` for the
   package stage, `eclipse-temurin:17-jre` for the run stage) and starts
   the app on `http://localhost:8080`.

2. Verify the base application is up:

   ```bash
   curl -i http://localhost:8080/api/programmes?storeId=store-1
   ```

   Expected: `200 OK` with an empty JSON array `[]` on a fresh H2
   in-memory database.

3. Create a programme, then exercise the harness-generated feature
   (`POST /api/programmes/{id}/templates`) end-to-end:

   ```bash
   # Create an ACTIVE programme
   curl -s -X POST http://localhost:8080/api/programmes \
     -H "Content-Type: application/json" \
     -d '{"name":"New Store Setup","storeId":"store-6","regionId":"region-3"}'
   # -> 201 Created, capture "id" from the response body as $PROJECT_ID

   # Apply the planogram template
   curl -i -X POST http://localhost:8080/api/programmes/$PROJECT_ID/templates \
     -H "Content-Type: application/json" \
     -d '{
           "templateName": "Standard Planogram",
           "tasks": [
             {"title":"Reset endcap 3","department":"Grocery","priority":"HIGH","category":"PLANOGRAM"},
             {"title":"Reset aisle 7","department":"Dairy","priority":"MEDIUM","category":"PLANOGRAM"}
           ]
         }'
   ```

   Expected: `202 Accepted`, response body echoes the programme with
   `storeId: "store-6"` — confirming the endpoint accepted the request
   and the service layer ran without a module-boundary or error-contract
   violation (both would have surfaced as a `4xx`/`5xx` from
   `GlobalExceptionHandler` instead).

## API validation (Postman)

`postman/StoreOps.postman_collection.json` covers all 13 endpoints plus
the error contract, and `postman/StoreOps.local.postman_environment.json`
holds the `baseUrl`/`storeId`/`regionId` values.

### GUI

1. Postman → **Import** → select both files from `postman/`.
2. Select the **StoreOps - local** environment (top right).
3. Open the collection → **Run** → keep the folder order (1–6) and run.

Requests chain through collection variables (`projectId`, `taskId`,
`staffId`), so folder order matters: folder 1 creates the programme that
folder 2 attaches tasks to and folder 5 aggregates.

### Headless (CI-friendly)

```bash
npm install -g newman
newman run postman/StoreOps.postman_collection.json \
  -e postman/StoreOps.local.postman_environment.json \
  --reporters cli,junit --reporter-junit-export target/newman-report.xml
```

`newman` exits non-zero if any assertion fails, so this can be dropped
into `.github/workflows/ci.yml` as a post-deploy smoke stage.

### What the assertions check

The tests assert resulting **state**, not just status codes — the same
standard `how-to-test/SKILL.md` imposes on the JUnit suite, and a direct
counter to client failure mode #3:

| Folder | Validates |
|---|---|
| 1. Programmes | create → 201 with `status: ACTIVE`; lookup; store filter; member added with role; template apply → **202** (not 200) because task creation is async |
| 2. Activities | `TODO`/`GENERAL`/`MEDIUM` defaults; `completedAt` stamped only on the transition into `DONE`; re-opening a `DONE` task → 409 |
| 3. Staff | role + store assignment persisted; duplicate email → 409 |
| 4. Alerts | list returns an array; `POST /api/alerts` returns 404/405 — proving no route exists to bypass the event bus (rule 2) |
| 5. Reports | `completionRate` equals `completedCount / taskCount`; `overdueByCategory` present; `POST` to a reports path → 404/405 (rule 5) |
| 6. Error contract | 400 `VALIDATION_ERROR`, 404 `NOT_FOUND`, 409 `CONFLICT`, and that the body is exactly `{code, message, timestamp}` with no leaked `trace`/`exception`/`path` fields (rule 3) |

### Two results that look like failures but are correct

- **`GET /api/alerts?staffId=staff-1` returns `[]`.** Notifications are
  raised only from domain events and no subscriber is wired yet.
- **Applying a template creates no `Task` rows.** `PROGRAMME_TEMPLATE_APPLIED`
  is emitted, but the `activities` listener is the open item behind sprint
  1's CONDITIONAL PASS (`.harness/reviews/sprint-1-evaluator-feedback.md`).
  Add the listener assertion to folder 1 when that sprint lands.

## Verification note for this submission

The machine this repository was authored on has Maven 3.9.5 but only a
JDK 1.8 toolchain, and no outbound network access. This project targets
Java 17, so neither `mvn verify` nor `docker compose up` could be
executed here — **the source under `src/` has not been compile-verified.**

The steps above are the exact commands to run once this repository is
pulled into an environment with JDK 17+, Docker, and network access
(needed for the first `mvn` dependency resolution inside the build
stage). Run `mvn verify` first: it executes the same Checkstyle,
SpotBugs, and JUnit gates the Evaluator relies on
(`.harness/reviews/sprint-1-evaluator-feedback.md`). Sprint 1's
CONDITIONAL PASS should not be treated as final until it passes there.

## Cloud deployment (not used for this submission)

AWS Elastic Beanstalk, Azure App Service/Container Apps, and GCP Cloud
Run are all viable for this Spring Boot jar with no code changes beyond
externalising the H2 URL if a persistent store is later required — H2
in-memory is appropriate for the capstone reference app but would not
survive a container restart in a real deployment.
