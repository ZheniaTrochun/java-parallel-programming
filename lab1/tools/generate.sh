#!/usr/bin/env bash

sbt assembly

java -jar target/scala-2.12/generation-tool-assembly-0.1.jar