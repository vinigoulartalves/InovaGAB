#!/usr/bin/env bash
# APENAS bancos de teste explícitos (prefixo inovagab_test). Nunca inovagab de desenvolvimento.
# Uso manual: bash scripts/reset-test-databases.sh
set -euo pipefail

if [[ "${CONFIRM_RESET_TEST_DB:-}" != "yes" ]]; then
  echo "Recusa: export CONFIRM_RESET_TEST_DB=yes para apagar bancos inovagab_test*" >&2
  exit 1
fi

MONGO_URI="${MONGODB_URI:-mongodb://127.0.0.1:27017/?replicaSet=rs0&directConnection=true}"

docker compose up -d mongo mongo-init 2>/dev/null || true

docker compose exec -T mongo mongosh "$MONGO_URI" --quiet --eval '
const dbs = db.adminCommand({ listDatabases: 1 }).databases
  .map(d => d.name)
  .filter(n => n.startsWith("inovagab_test"));
for (const name of dbs) {
  print("Dropping test db: " + name);
  db.getSiblingDB(name).dropDatabase();
}
print("Done. Banco inovagab (dev) não foi alterado.");
'
