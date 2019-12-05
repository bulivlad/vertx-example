#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(dirname "$0")"
TAG="$(git rev-parse --short=10 HEAD:../src)"

case $1 in
  start)
    TAG=${TAG} docker-compose -f ${SCRIPT_DIR}/../docker/server/docker-compose.yml up -d vertx-server
    ;;
  stop)
    TAG=${TAG} docker-compose -f ${SCRIPT_DIR}/../docker/server/docker-compose.yml stop vertx-server
    ;;
  clear)
    TAG=${TAG} docker-compose -f ${SCRIPT_DIR}/../docker/server/docker-compose.yml down
    ;;
  *)
    echo "Incorrect argument"
    ;;
esac
