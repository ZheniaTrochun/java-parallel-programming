#!/bin/bash

set -x
set -m

echo "cool"

/entrypoint.sh couchbase-server &

sleep 15


couchbase-cli cluster-init --cluster-username local --cluster-password localpass
couchbase-cli user-manage -c localhost:8091 --username local --password localpass --set --rbac-username admin --rbac-password admin123 --auth-domain local --roles admin
couchbase-cli bucket-create -c localhost:8091 --bucket-type couchbase --bucket-ramsize 500 -u admin -p admin123 --bucket=DataObject --wait
cbq -u local -p localpass --script="create primary index on \`DataObject\`;"


fg 1
