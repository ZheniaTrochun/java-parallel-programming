#!/bin/bash

set -x
set -m

echo "cool"

/entrypoint.sh couchbase-server &

sleep 50


couchbase-cli cluster-init --cluster-username local --cluster-password localpass --wait
couchbase-cli user-manage -c 0.0.0.0:8091 --username local --password localpass --set --rbac-username admin --rbac-password admin123 --auth-domain local --roles admin --wait
couchbase-cli bucket-create -c 0.0.0.0:8091 --bucket-type couchbase --bucket-ramsize 500 -u admin -p admin123 --bucket=DataObject --wait
# cbq -e 0.0.0.0:8093 -u admin -p admin123 --script="create primary index on \`DataObject\`;"
cbq -u admin -p admin123 --script="create primary index on \`DataObject\`;"


fg 1
