#!/usr/bin/env bash

docker build -t generation-tool .

docker run --rm --name=generation-tool -d -v ../data:/app/data generation-tool