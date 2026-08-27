#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

usage() {
  cat <<'EOF'
Usage:
  ./scripts/logs.sh all
  ./scripts/logs.sh app
  ./scripts/logs.sh refs
  ./scripts/logs.sh kafka
  ./scripts/logs.sh db
  ./scripts/logs.sh errors
  ./scripts/logs.sh trade <TRADE_ID>

Commands:
  all           Follow all compose service logs
  app           Follow main app logs (fidstp2-app)
  refs          Follow reference service logs (counterparty + currency pair)
  kafka         Follow kafka broker logs
  db            Follow main postgres logs
  errors        Follow main app logs filtered to WARN/ERROR/retry/DLQ
  trade <id>    Follow main app logs filtered by trade id
EOF
}

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required" >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "docker compose is required" >&2
  exit 1
fi

cmd="${1:-}"
case "$cmd" in
  all)
    exec docker compose logs -f --tail=200
    ;;
  app)
    exec docker compose logs -f --tail=300 fidstp2-app
    ;;
  refs)
    exec docker compose logs -f --tail=300 counterparty-service currency-pair-service
    ;;
  kafka)
    exec docker compose logs -f --tail=200 kafka
    ;;
  db)
    exec docker compose logs -f --tail=200 postgres
    ;;
  errors)
    exec docker compose logs -f --tail=500 fidstp2-app | grep -E "WARN|ERROR|DLQ|retry|Retry|dead[-_ ]letter"
    ;;
  trade)
    trade_id="${2:-}"
    if [[ -z "$trade_id" ]]; then
      echo "trade id is required" >&2
      usage
      exit 1
    fi
    exec docker compose logs -f --tail=500 fidstp2-app | grep --line-buffered -F "$trade_id"
    ;;
  -h|--help|help|"")
    usage
    ;;
  *)
    echo "Unknown command: $cmd" >&2
    usage
    exit 1
    ;;
esac

