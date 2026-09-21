#!/usr/bin/env bash
set -euo pipefail

MONGO_HOST="${MONGO_HOST:-mongo}"
MONGO_PORT="${MONGO_PORT:-27017}"
RS_NAME="${RS_NAME:-rs0}"
RS_MEMBER_HOST="${RS_MEMBER_HOST:-${MONGO_HOST}:${MONGO_PORT}}"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-60}"
# Antes do rs.initiate(), o mongod ainda não tem PRIMARY — directConnection evita
# server selection de ~30s por tentativa (parece “travado” no compose).
MONGO_URI="mongodb://${MONGO_HOST}:${MONGO_PORT}/?directConnection=true&serverSelectionTimeoutMS=5000"

echo "Waiting for mongod at ${MONGO_HOST}:${MONGO_PORT} (directConnection)..."
for i in $(seq 1 "$MAX_ATTEMPTS"); do
  if mongosh "$MONGO_URI" --quiet --eval "db.adminCommand('ping').ok" 2>/dev/null | grep -qE '[01]'; then
    echo "mongod is reachable (attempt ${i})."
    break
  fi
  if [ "$i" -eq "$MAX_ATTEMPTS" ]; then
    echo "ERROR: mongod not reachable at ${MONGO_HOST}:${MONGO_PORT}" >&2
    exit 1
  fi
  if [ $((i % 5)) -eq 0 ]; then
    echo "Still waiting for mongod... (attempt ${i}/${MAX_ATTEMPTS})"
  fi
  sleep 2
done

mongosh "$MONGO_URI" --quiet <<EOF
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
    members: [{ _id: 0, host: '${RS_MEMBER_HOST}' }]
  });
}
EOF

echo "Waiting for PRIMARY election..."
for i in $(seq 1 "$MAX_ATTEMPTS"); do
  STATE=$(mongosh "$MONGO_URI" --quiet --eval "try { db.hello().isWritablePrimary ? 'true' : 'false' } catch(e) { 'false' }" | tr -d '\r\n')
  if [ "$STATE" = "true" ]; then
    echo "PRIMARY elected."
    exit 0
  fi
  if [ $((i % 5)) -eq 0 ]; then
    echo "Still waiting for PRIMARY... (attempt ${i}/${MAX_ATTEMPTS})"
  fi
  sleep 2
done

echo "ERROR: PRIMARY not elected in time." >&2
exit 1
