#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(dirname "$0")"
PLATFORM_GIT_TAG="$(git rev-parse --short=10 HEAD:../docker/platform)"
JAVA_GIT_TAG="$(git rev-parse --short=10 HEAD:../docker/java)"
SERVER_GIT_TAG="$(git rev-parse --short=10 HEAD:../src)"

function build_platform_image(){
  pushd "${SCRIPT_DIR}"
  pushd "../docker/platform"
  docker build . -t bootcamp/platform-image:${PLATFORM_GIT_TAG} --build-arg VERSION=${PLATFORM_GIT_TAG}
}

function build_java_image(){
  popd
  pushd "../docker/java"
  docker build --build-arg VERSION=${JAVA_GIT_TAG} --build-arg BASE_TAG=${PLATFORM_GIT_TAG} . -t bootcamp/java-image:${JAVA_GIT_TAG}
}

function build_server_image(){
  popd
  pushd "../docker/server"
  CURRENT_DIR="$(pwd)"
  BUILD_DIR="build/libs"
  docker build -f ./Dockerfile ../../ --build-arg VERSION=${SERVER_GIT_TAG} --build-arg BASE_TAG=${JAVA_GIT_TAG} --build-arg BUILD_DIR=${BUILD_DIR} -t bootcamp/server-image:${SERVER_GIT_TAG}
}

build_platform_image
build_java_image
build_server_image