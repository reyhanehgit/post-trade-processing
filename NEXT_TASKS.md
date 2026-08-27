m# NEXT_TASKS

Lightweight execution board for the next delivery slices.

Last updated: 2026-08-27

## TODO

- [ ] **Trade Read APIs (Phase 7 core)**
  - Implement `GET /api/trades/{tradeId}`
  - Implement `GET /api/trades/{tradeId}/history`
  - Implement `GET /api/trades/{tradeId}/status`
  - Add integration tests for success/not-found/error payloads
  - Update `README.md` and `USAGE_E2E.md` with curl examples

- [ ] **Replay API + Idempotent Guardrails**
  - Implement `POST /api/trades/{tradeId}/replay`
  - Enforce replay eligibility and duplicate suppression
  - Persist replay audit entries in `processing_history`
  - Add integration tests for replay happy path and duplicate replay requests

- [ ] **Enrichment Cache Refresh/Invalidation**
  - Add scheduled refresh or admin-triggered refresh
  - Keep lookup path resilient when refresh fails
  - Add refresh metrics (attempts/success/failure)
  - Add tests for stale-to-fresh transition behavior

- [ ] **Outbox Reliability Depth Tests**
  - Add integration tests for `NEW -> PENDING_RETRY -> PUBLISHED`
  - Add integration tests for max-retry dead-letter transition
  - Assert `retry_count`, `next_retry_at`, `last_error`, `failed_at`

- [ ] **Observability Expansion (Phase 8)**
  - Add throughput, end-to-end latency, and failure-reason metrics
  - Add alert signals for DLQ growth and outbox retry backlog
  - Document metric names and expected operating thresholds

- [ ] **Operational Runbook Hardening**
  - Add incident playbooks: Kafka lag, DLQ spike, reference service outage
  - Add triage command snippets aligned with `docker-compose.yml`
  - Add rollback/recovery steps for replay and outbox retry operations

## IN PROGRESS

- (none)

## DONE

- [x] Phase 6 reliability baseline (consumer retry, DLQ fallback, outbox retry scheduler)
- [x] Remote enrichment wiring enabled by default
- [x] Startup cache warm-up for remote counterparty/currency pair
- [x] Startup cache preload metrics added to `ReliabilityMetrics`
- [x] End-to-end usage guide created in `USAGE_E2E.md`
- [x] README alignment pass with current project state

## Suggested Pick Order

1. Trade Read APIs
2. Replay API
3. Outbox Reliability Depth Tests
4. Enrichment Cache Refresh/Invalidation
5. Observability Expansion
6. Operational Runbook Hardening

## Ready-to-Start Checklist (for whoever picks next task)

- [ ] Confirm acceptance criteria in `IMPLEMENTATION_PHASES.md`
- [ ] Add/extend tests first for target behavior
- [ ] Keep docs updated in same PR (`README.md`, `USAGE_E2E.md`, `NEXT_TASKS.md`)
- [ ] Run `./gradlew test` before handoff

