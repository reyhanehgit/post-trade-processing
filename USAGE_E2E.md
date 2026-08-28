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
RAW_MSG=$(cat <<XML
<tradeEnvelope xmlns="http://example.org/fidstp2/trade" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <trade xsi:type="FxOptionTradeType">
	<tradeId>${TRADE_ID}</tradeId>
	<externalTradeId>EXT-${TRADE_ID}</externalTradeId>
	<productType>FX_OPTION</productType>
	<currencyPair>EUR/USD</currencyPair>
	<notionalAmount>2500000</notionalAmount>
	<notionalCurrency>EUR</notionalCurrency>
	<buySell>BUY</buySell>
	<tradeDate>2026-08-26</tradeDate>
	<valueDate>2026-08-29</valueDate>
	<optionType>CALL</optionType>
	<strikePrice>1.2500</strikePrice>
	<expiryDate>2026-10-01</expiryDate>
	<optionStyle>VANILLA</optionStyle>
  </trade>
  <counterparty>
	<counterpartyId>CP-1</counterpartyId>
	<legalEntityId>LE-1</legalEntityId>
	<sourceSystem>OMS</sourceSystem>
  </counterparty>
</tradeEnvelope>
XML
)
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

If you see `ERROR: column "retry_count" does not exist`, your DB is on an older migration set. Rebuild/restart the main app so Flyway applies `V5__harden_outbox_retries.sql`:

```bash
./gradlew build -x test
docker compose up -d --build fidstp2-app
```

## 6) Verify DLQ behavior with an invalid trade

This message is intentionally malformed XML and should fail processing.

```bash
BAD_ID="T-BAD-$(date +%s)"
BAD_MSG=$(cat <<XML
<tradeEnvelope>
  <trade>
	<tradeId>${BAD_ID}</tradeId>
XML
)
printf '%s\n' "$BAD_MSG" | docker exec -i fidstp2-kafka kafka-console-producer --bootstrap-server localhost:9092 --topic fx.option.trade.raw
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

## 8.1) Easier log viewing helper

Use the helper script in `scripts/logs.sh` for common log views:

```bash
./scripts/logs.sh app
```

```bash
./scripts/logs.sh errors
```

```bash
./scripts/logs.sh trade T-E2E-12345
```

## 8.2) Local log files on your machine

Because `docker-compose.yml` mounts log directories for each app service, you can read logs directly from this repo:

- `logs/fidstp2-app/fidstp2-app.log`
- `logs/counterparty-service/counterparty-service.log`
- `logs/currency-pair-service/currency-pair-service.log`

Live tail examples:

```bash
tail -f logs/fidstp2-app/fidstp2-app.log
```

```bash
tail -f logs/counterparty-service/counterparty-service.log
```

```bash
tail -f logs/currency-pair-service/currency-pair-service.log
```

Quick filter examples:

```bash
grep -E "ERROR|WARN|DLQ|retry" logs/fidstp2-app/fidstp2-app.log | tail -n 50
```

```bash
grep "T-E2E-12345" logs/fidstp2-app/fidstp2-app.log
```

