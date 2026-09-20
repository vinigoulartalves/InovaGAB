#!/usr/bin/env bash
set -euo pipefail

MONGO_HOST="${MONGO_HOST:-mongo}"
MONGO_PORT="${MONGO_PORT:-27017}"
RS_NAME="${RS_NAME:-rs0}"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-60}"

echo "Waiting for mongod at ${MONGO_HOST}:${MONGO_PORT}..."
for i in $(seq 1 "$MAX_ATTEMPTS"); do
  if mongosh --host "${MONGO_HOST}" --port "${MONGO_PORT}" --quiet --eval "db.adminCommand('ping').ok" 2>/dev/null | grep -q 1; then
    break
  fi
  sleep 2
done

mongosh --host "${MONGO_HOST}" --port "${MONGO_PORT}" --quiet <<EOF
try {
  const status = rs.status();
  if (status.ok === 1) {
    print('Replica set already initialized.');
    quit(0);
  }
} catch (e) {
  print('Initializing replica set ${RS_NAME}...');
  rs.initiate({
    _id: '${RS_NAME}',
    members: [{ _id: 0, host: '${MONGO_HOST}:${MONGO_PORT}' }]
  });
}
EOF

echo "Waiting for PRIMARY election..."
for i in $(seq 1 "$MAX_ATTEMPTS"); do
  STATE=$(mongosh --host "${MONGO_HOST}" --port "${MONGO_PORT}" --quiet --eval "try { rs.isMaster().ismaster } catch(e) { false }")
  if [ "$STATE" = "true" ]; then
    echo "PRIMARY elected."
    exit 0
  fi
  sleep 2
done

echo "ERROR: PRIMARY not elected in time." >&2
exit 1
