#!/usr/bin/env bash

docker build -t generation-tool .

docker run --rm --name=generation-tool -d -v /root/data:/app/generated generation-tool