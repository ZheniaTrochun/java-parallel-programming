#!/bin/bash

docker build -t generation-tool ./tools
docker run -v /root/java-parallel-programming/couchbase-document-loader/java/reactive-data-service:/app/generated generation-tool
echo "data generated"

docker-compose build
echo "build funished"

docker-compose up -d
echo "starting up..."
