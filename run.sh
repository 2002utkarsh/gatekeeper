#!/bin/sh
if [ -z "$1" ]; then
  echo "Usage: ./run.sh <url> [--check-cors] [--check-actuator] [--check-sql]"
  exit 2
fi
java -jar target/gatekeeper-1.0.jar --url "$@"