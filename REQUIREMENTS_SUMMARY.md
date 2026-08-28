# REQUIREMENTS SUMMARY

This is a simple, high-level summary of what this project is supposed to do.

## 1) Business goal

Build a post-trade FX processing system that:

- receives raw FIX-style trade messages
- parses them into Java domain objects
- validates the trade content
- enriches trades with reference data
- persists trade lifecycle/audit data
- publishes processed events to downstream Kafka topics
- handles failures using retry, DLQ, and outbox patterns

## 2) Main functional requirements

### Inbound trade handling
- Consume raw messages from Kafka topic `fx.option.trade.raw`
- Support both pipe-delimited (`|`) and SOH-delimited (`\u0001`) FIX-like payloads
- Parse FIX tags into internal trade objects

### Validation
- Validate required trade fields
- Validate business rules such as:
  - product type
  - currency pair format
  - notional amount
  - strike price
  - expiry date
  - multi-leg rules where applicable
- Reject invalid trades with clear error messages and audit history where applicable

### Enrichment
- Enrich trades using reference data:
  - counterparty
  - currency pair
  - legal entity
  - settlement instruction
- Support both:
  - local/in-memory or local DB-backed enrichment
  - remote microservice enrichment over HTTP
- Cache remote reference data locally for faster lookups

### Processing
- Process validated/enriched trades into a final internal result
- Publish downstream processed events
- Keep audit trail of key processing steps

### Persistence
- Persist data into PostgreSQL
- Store:
  - trades
  - trade legs
  - processing history
  - outbox events

### Publication
- Publish processed trade events to Kafka topic `fx.option.trade.processed`
- Use trade id as Kafka key for ordering

### Error handling and recovery
- Retry consumer failures with backoff
- Send exhausted consumer failures to `fx.option.trade.dlq`
- Use transactional outbox pattern for safer event handoff
- Retry failed outbox publications using scheduled republisher logic
- Mark permanently failed outbox events as dead-lettered

## 3) Main non-functional requirements

- Use **Java 17**
- Use **Spring Boot**
- Use **PostgreSQL** for persistence
- Use **Kafka** for messaging
- Be modular and maintainable
- Support extension to more product types over time
- Expose health/metrics endpoints for operations
- Be runnable locally with Docker Compose

## 4) Current architecture requirements

The system currently consists of:

- **Main app (`fidstp2`)**
  - core processing pipeline
  - Kafka consumer/publisher
  - main PostgreSQL database
- **Counterparty service**
  - reference data microservice
  - own PostgreSQL database
- **Currency-pair service**
  - reference data microservice
  - own PostgreSQL database
- **Kafka + DLQ + Kafka UI**

## 5) Main database tables

The main app needs these persistence tables:

- `trade`
- `trade_leg`
- `processing_history`
- `outbox_event`

## 6) Main API requirements

### Already present
- Reference-data APIs for counterparties
- Reference-data APIs for currency pairs

### Still expected / future scope
- `GET /api/trades/{tradeId}`
- `GET /api/trades/{tradeId}/history`
- `GET /api/trades/{tradeId}/status`
- `POST /api/trades/{tradeId}/replay`

## 7) Reliability requirements

The system should:

- not lose processed events when publication temporarily fails
- support retry with configurable backoff
- support DLQ handling for terminal failures
- keep retry metadata in the database
- expose metrics for retries, DLQ, and cache preload behavior

## 8) Observability requirements

The system should provide:

- actuator health endpoint
- metrics endpoint
- Prometheus-friendly metrics
- local file logs and container logs
- enough audit history to understand what happened to a trade

## 9) Product-type extensibility requirement

The design should support adding more product types without rewriting the full pipeline.

Current strategy-based extension points now exist for:

- FIX mapping
- validation
- enrichment
- processing

This means future product types should be added by plugging in product-specific strategies.

## 10) Important remaining requirements

The biggest remaining project requirements are:

1. full trade query APIs
2. replay API with idempotency safeguards
3. deeper observability and alerting
4. operational runbooks
5. stronger multi-product support with true product-specific domain behavior

## 11) Best docs to read next

If you want more detail, use these files:

- `README.md` — current working overview
- `USAGE_E2E.md` — how to run and test locally
- `ARCHITECTURE.md` — diagrams and object relationships
- `IMPLEMENTATION_PHASES.md` — phased plan and current progress
- `NEXT_TASKS.md` — active backlog
- `HELP.md` — detailed original requirements source

