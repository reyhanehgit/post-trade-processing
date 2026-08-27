# FIDSTP2 End-to-End Usage (Kafka -> Processing -> DB -> Downstream)

This guide gives you a repeatable way to test the full flow locally.

## 1) Start the full stack

```bash
docker compose up -d --build
```

## 2) Quick health checks

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

```bash
curl -s http://localhost:8080/actuator/health
```

```bash
curl -s http://localhost:8888/api/reference/counterparties
```

```bash
curl -s http://localhost:8889/api/reference/currency-pairs
```

## 2.1) Open Kafka UI

Kafka UI is exposed from Docker compose on host port `8081`.

- Open in browser: `http://localhost:8081`
- Cluster name in UI: `fidstp2-local`

Optional quick HTTP check:

```bash
curl -i http://localhost:8081
```

## 3) Send a valid FX option trade to Kafka

Use one unique trade id each run:

```bash
TRADE_ID="T-E2E-$(date +%s)"
RAW_MSG="11=${TRADE_ID}|37=EXT-${TRADE_ID}|20000=OPTION|55=EUR/USD|15=EUR|38=2500000|54=1|75=20260826|64=20260829|20001=CALL|44=1.2500|20003=20261001|20004=VANILLA|1=CP-1|20006=LE-1|49=OMS|"
printf '%s\n' "$RAW_MSG" | docker exec -i fidstp2-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic fx.option.trade.raw
```

## 4) Check downstream topic for processed event

```bash
docker exec fidstp2-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic fx.option.trade.processed --from-beginning --max-messages 1 --timeout-ms 15000
```

Expected payload shape:

```json
{"eventVersion":"1.0","tradeId":"T-E2E-...","status":"PUBLISHED","publishedAt":"..."}
```

## 5) Check what got persisted in the main DB

If you exported `TRADE_ID` in step 3, use these directly.

```bash
docker exec -i fidstp2-postgres psql -U postgres -d fidstp2 -c "SELECT trade_id, external_trade_id, product_type, currency_pair, counterparty_id, processing_status FROM trade WHERE trade_id = '$TRADE_ID';"
```

```bash
docker exec -i fidstp2-postgres psql -U postgres -d fidstp2 -c "SELECT trade_id, status, stage, message, created_at FROM processing_history WHERE trade_id = '$TRADE_ID' ORDER BY created_at;"
```

```bash
docker exec -i fidstp2-postgres psql -U postgres -d fidstp2 -c "SELECT aggregate_id, event_type, status, retry_count, last_error, created_at, published_at FROM outbox_event WHERE aggregate_id = '$TRADE_ID' ORDER BY created_at;"
```

## 6) Verify DLQ behavior with an invalid trade

This message is intentionally incomplete and should fail processing.

```bash
BAD_ID="T-BAD-$(date +%s)"
printf '%s\n' "11=${BAD_ID}|37=EXT-${BAD_ID}|55=EUR/USD|" | docker exec -i fidstp2-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic fx.option.trade.raw
```

Check DLQ topic:

```bash
docker exec fidstp2-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic fx.option.trade.dlq --from-beginning --max-messages 1 --timeout-ms 20000
```

Check audit trail for failed trade:

```bash
docker exec -i fidstp2-postgres psql -U postgres -d fidstp2 -c "SELECT trade_id, status, stage, message, created_at FROM processing_history WHERE trade_id = '$BAD_ID' ORDER BY created_at;"
```

## 7) What success looks like

- Valid message appears on `fx.option.trade.processed`.
- A row exists in `trade` with your `trade_id`.
- `processing_history` contains processing steps for that `trade_id`.
- `outbox_event` has the event for that `trade_id` with `PUBLISHED` (or retry metadata if transient failures occurred).
- Invalid message ends up on `fx.option.trade.dlq` after retry policy is exhausted.

## 8) Useful troubleshooting commands

```bash
docker logs --tail=200 fidstp2-app
```

```bash
docker logs --tail=200 counterparty-service
```

```bash
docker logs --tail=200 currency-pair-service
```

```bash
docker exec fidstp2-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

