#!/usr/bin/env bash
set -e
shopt -s nullglob

# we want the JVM to be PID 1, so should replace the entrypoint
exec java ${JAVA_OPTS} -jar ${DIGILEAP_DIR}/lib/${SERVICE_NAME}.jar "$@"

shopt -u nullglob
