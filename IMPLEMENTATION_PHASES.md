# Implementation Phases

Based on the requirements in `HELP.md`, this plan breaks the build into practical delivery phases with clear outputs.

## Phase 0 - Project Foundation

**Goal:** Bootstrap a stable local dev and test setup.

### Scope
- Confirm Java 17 + Gradle wrapper setup.
- Add baseline Spring Boot modules (web, validation, data-jpa, kafka, actuator).
- Add test stack (JUnit 5, Mockito, Testcontainers, WireMock).
- Add Flyway and PostgreSQL config placeholders.
- Define package structure by layer (`domain`, `parser`, `validation`, `enrichment`, `processing`, `persistence`, `messaging`, `api`).

### Deliverables
- Build runs with `./gradlew test`.
- Application starts with a health endpoint.
- CI-ready Gradle project skeleton.

## Phase 1 - Domain + Contracts

**Goal:** Lock down the core model and interfaces before implementation details.

### Scope
- Implement core entities and enums:
  - `Trade`
  - `FxOptionTrade`
  - `TradeLeg`
  - `OptionType`, `OptionStyle`, `BuySell`
- Add DTO/event contracts for inbound FIX and outbound downstream messages.
- Define key interfaces:
  - `FixMessageParser`
  - `TradeValidator`
  - `TradeProcessor`
  - enrichment service contracts

### Deliverables
- Compilable domain model and interfaces.
- Unit tests for domain invariants (constructors, value checks).

## Phase 2 - Parsing + Validation

**Goal:** Convert raw FIX into domain objects and reject bad data early.

### Scope
- Implement FIX-tag parser to map raw messages to `FxOptionTrade`.
- Keep FIX mapping isolated from business logic.
- Implement `FxOptionTradeValidator` with required-field and consistency rules.
- Add clear validation error model for history/logging.

### Deliverables
- Parser unit tests with valid/invalid FIX samples.
- Validator unit tests for mandatory fields and cross-field checks.

## Phase 3 - Enrichment Services

**Goal:** Resolve external reference data needed for processing.

### Scope
- Implement:
  - `CounterpartyService`
  - `CurrencyPairService`
  - `LegalEntityService`
- Add caching strategy (hit/miss metrics-ready).
- Add timeout/error handling policy for external lookups.

### Deliverables
- Enrichment integration tests using mocked external systems.
- Caching behavior tests.

## Phase 4 - Persistence + Migrations

**Goal:** Persist trade lifecycle and support replay/audit.

### Scope
- Create Flyway migrations for:
  - `trade`
  - `trade_leg`
  - `processing_history`
  - `outbox_event`
- Add JPA entities + repositories.
- Model processing statuses and timestamps.

### Deliverables
- DB schema migrations versioned and repeatable.
- Repository tests (prefer Testcontainers PostgreSQL).

## Phase 5 - Processing Pipeline + Idempotency

**Goal:** Build deterministic orchestration from parse to persist.

### Scope
- Implement `FxOptionTradeProcessor` orchestration flow:
  - parse -> validate -> enrich -> persist -> produce event
- Add idempotency key strategy (tradeId + version/message fingerprint).
- Add processing history updates for each stage.

### Deliverables
- End-to-end service test for happy path and duplicate-message handling.
- Documented idempotency behavior.

## Phase 6 - Kafka + Reliability Patterns

**Goal:** Integrate event streaming with retry safety and delivery guarantees.

### Scope
- Implement Kafka consumer for inbound raw trade messages.
- Implement Kafka publisher using `tradeId` as key for ordering.
- Add retry with exponential backoff.
- Add DLQ publishing and metadata.
- Implement Transactional Outbox pattern for robust handoff.

### Deliverables
- Integration tests for retry, DLQ, and ordered publish behavior.
- Failure injection tests for transient/persistent errors.

## Phase 7 - REST APIs + Replay Operations

**Goal:** Expose operational and audit capabilities.

### Scope
- Implement APIs:
  - `GET /api/trades/{tradeId}`
  - `GET /api/trades/{tradeId}/history`
  - `GET /api/trades/{tradeId}/status`
  - `POST /api/trades/{tradeId}/replay`
- Add input validation and error responses.
- Ensure replay is idempotent and auditable.

### Deliverables
- Controller/service tests.
- API contract examples and error scenarios.

## Phase 8 - Observability + Hardening

**Goal:** Make the system production-operable.

### Scope
- Add structured logs (`tradeId`, `correlationId`, `stage`, `result`).
- Add metrics:
  - `trades_received_total`
  - `trades_processed_total`
  - `trades_failed_total`
  - cache hit/miss
  - enrichment latency
- Add alert-ready indicators (DLQ growth, retry exhaustion, parse failures).
- Performance and soak tests.

### Deliverables
- Dashboards/metric wiring (or metric catalog if dashboards are out of scope).
- Operational runbook for common failure modes.

## Suggested Milestones

- **M1 (Phases 0-2):** Can parse/validate trades with tests.
- **M2 (Phases 3-5):** Can process, enrich, persist, and handle duplicates.
- **M3 (Phases 6-7):** Kafka reliability + API + replay complete.
- **M4 (Phase 8):** Production readiness and observability complete.

## Definition of Done (Per Phase)

- All new code has unit/integration tests.
- `./gradlew test` passes.
- No critical static analysis/lint issues.
- Documentation updated for behavior and failure handling.

## Current State Snapshot (August 2026)

Use this as the practical handoff status for the current repository state.

- **Phase 0:** complete
- **Phase 1:** complete
- **Phase 2:** complete
- **Phase 3:** mostly complete (remote/local enrichment + startup cache warmup in place; cache refresh controls pending)
- **Phase 4:** complete (trade/history/outbox plus retry-hardening migration)
- **Phase 5:** mostly complete (core orchestration stable; broader replay/idempotency workflow still pending)
- **Phase 6:** mostly complete (retry, DLQ, outbox republish, reliability metrics)
- **Phase 7:** early (reference-data APIs exist; trade read/status/history/replay APIs pending)
- **Phase 8:** in progress (actuator + reliability/caching metrics exist; broader business SLO metrics and runbook depth pending)

## Prioritized Pickup Backlog (With Acceptance Criteria)

Pick work top-to-bottom. Each item is done only when all acceptance checks pass.

### 1) Trade read APIs (Phase 7 core)

**Scope:**
- Implement `GET /api/trades/{tradeId}`
- Implement `GET /api/trades/{tradeId}/history`
- Implement `GET /api/trades/{tradeId}/status`

**Acceptance criteria:**
- [ ] Returns `200` with stable JSON contracts for existing trades.
- [ ] Returns `404` with consistent error payload for unknown `tradeId`.
- [ ] Includes controller + service + repository test coverage.
- [ ] Added examples in `README.md` and/or `USAGE_E2E.md`.

### 2) Replay API with idempotent controls

**Scope:**
- Implement `POST /api/trades/{tradeId}/replay`.
- Enforce replay guardrails (eligibility status, duplicate suppression, audit reason).

**Acceptance criteria:**
- [ ] Replay request writes explicit audit record to `processing_history`.
- [ ] Duplicate replay requests do not produce duplicate terminal outcomes.
- [ ] Failure scenarios are mapped to deterministic API errors.
- [ ] Integration tests cover happy-path and duplicate replay behavior.

### 3) Enrichment cache refresh/invalidation

**Scope:**
- Add refresh strategy for remote `CounterpartyService` and `CurrencyPairService` caches.
- Optionally provide admin-triggered refresh endpoint or scheduled refresh job.

**Acceptance criteria:**
- [ ] Cache refresh behavior is configurable (interval or endpoint toggle).
- [ ] Refresh failures do not break request-path enrichment.
- [ ] Metrics capture refresh attempts/success/failure counts.
- [ ] Tests prove stale entries are refreshed and lookups remain resilient.

### 4) Outbox reliability depth tests

**Scope:**
- Add deeper integration coverage for retry exhaustion and dead-letter transitions.
- Validate `next_retry_at`, `retry_count`, and terminal state transitions.

**Acceptance criteria:**
- [ ] Integration tests verify transition path `NEW -> PENDING_RETRY -> PUBLISHED`.
- [ ] Integration tests verify terminal dead-letter transition after max attempts.
- [ ] Assertions include persisted retry metadata in `outbox_event`.
- [ ] No regression in existing DLQ integration tests.

### 5) Observability expansion (Phase 8)

**Scope:**
- Add business metrics for throughput/latency/failures by reason.
- Add alert-ready indicators for DLQ growth and retry backlog.

**Acceptance criteria:**
- [ ] Metrics exposed through `/actuator/metrics` and documented.
- [ ] At least one end-to-end latency metric from consume to publish.
- [ ] Failure counters broken down by validation, enrichment, and publish failures.
- [ ] Ops quick-check includes metric names and expected thresholds.

### 6) Operational runbook hardening

**Scope:**
- Expand incident procedures for Kafka lag, DLQ spikes, and reference-service outage.
- Add concrete command snippets and triage flow.

**Acceptance criteria:**
- [ ] Runbook has step-by-step diagnosis + mitigation for each major incident.
- [ ] Includes DB/Kafka commands that match current docker-compose service names.
- [ ] Includes rollback/recovery notes for replay and outbox retry operations.
- [ ] Cross-linked from `README.md` and `USAGE_E2E.md`.

## Week-by-Week Execution Plan (10 Weeks)

Assumption: single engineer full-time (or equivalent team capacity). If team size differs, use this as dependency order and rebalance parallel tracks.

### Week 1 - Foundation and Build Stability

- **Primary phases:** Phase 0
- **Estimated effort:** 4-5 dev-days
- **Focus:** baseline project setup, dependency wiring, package structure, health check, and green test pipeline.
- **Dependencies:** none
- **Exit criteria:** app boots, `./gradlew test` green, baseline config committed.

### Week 2 - Domain Model and Interfaces

- **Primary phases:** Phase 1
- **Estimated effort:** 4-5 dev-days
- **Focus:** entities/enums/contracts/interfaces finalized; initial unit tests for invariants.
- **Dependencies:** Week 1
- **Exit criteria:** domain and interfaces stable enough for parser/validator implementation.

### Week 3 - FIX Parsing

- **Primary phases:** Phase 2 (parser slice)
- **Estimated effort:** 3-4 dev-days
- **Focus:** FIX tag mapping, parser error handling model, valid/invalid sample coverage.
- **Dependencies:** Week 2
- **Exit criteria:** parser converts raw messages to `FxOptionTrade` with robust tests.

### Week 4 - Validation Layer

- **Primary phases:** Phase 2 (validator slice)
- **Estimated effort:** 3-4 dev-days
- **Focus:** business validation rules, cross-field checks, failure codes/messages.
- **Dependencies:** Week 3
- **Exit criteria:** validator blocks malformed/inconsistent trades with deterministic errors.

### Week 5 - Persistence and Schema

- **Primary phases:** Phase 4
- **Estimated effort:** 4-5 dev-days
- **Focus:** Flyway migrations, JPA entities/repositories, Testcontainers DB tests.
- **Dependencies:** Week 2 (domain), Week 1 (foundation)
- **Exit criteria:** trade lifecycle data persisted/retrieved correctly with migration history.

### Week 6 - Enrichment Services

- **Primary phases:** Phase 3
- **Estimated effort:** 4-5 dev-days
- **Focus:** external reference integrations, caching, timeouts/fallback behavior.
- **Dependencies:** Week 2, Week 5 for persistence-linked fields
- **Exit criteria:** enrichment services integrated and covered with mocked integration tests.

### Week 7 - Processing Orchestration and Idempotency

- **Primary phases:** Phase 5
- **Estimated effort:** 4-5 dev-days
- **Focus:** parse->validate->enrich->persist->event flow, idempotency keys, processing history stages.
- **Dependencies:** Weeks 3-6
- **Exit criteria:** happy path + duplicate message flows validated end-to-end.

### Week 8 - Kafka Reliability (Retry, DLQ, Outbox)

- **Primary phases:** Phase 6
- **Estimated effort:** 5 dev-days
- **Focus:** consumer/publisher wiring, keyed publishing by `tradeId`, exponential backoff, DLQ routing, transactional outbox.
- **Dependencies:** Week 7 and Week 5
- **Exit criteria:** failure injection tests pass for transient retry and terminal DLQ cases.

### Week 9 - REST APIs and Replay Operations

- **Primary phases:** Phase 7
- **Estimated effort:** 4-5 dev-days
- **Focus:** read/status/history/replay endpoints, replay guards, API error contracts.
- **Dependencies:** Week 7 and Week 8
- **Exit criteria:** API tests pass; replay is auditable and idempotent.

### Week 10 - Observability and Hardening

- **Primary phases:** Phase 8
- **Estimated effort:** 4-5 dev-days
- **Focus:** structured logging, metrics, alert signals, performance/soak verification, runbook completion.
- **Dependencies:** Weeks 8-9
- **Exit criteria:** operational visibility complete and production-readiness checklist satisfied.

## Parallelization Opportunities

- Weeks 5-6 can partially overlap if one stream handles DB/migrations while another builds enrichment adapters.
- Weeks 8-9 can overlap for API read endpoints while reliability features are finalized, but replay should wait for idempotency + outbox stability.

## Risk-Adjusted Buffer

- Reserve 10-15% schedule buffer (about 1 to 1.5 weeks) for integration instability in Kafka, external enrichment contracts, and replay semantics.
- If timeline is strict, de-scope non-critical observability extras first (keep core logs/metrics/alerts).

## Weekly Demo Checklist

- Demo one end-to-end user-visible capability every week.
- Keep a running test matrix (unit, integration, failure paths, replay paths).
- Capture known technical debt explicitly with owner and target week.

