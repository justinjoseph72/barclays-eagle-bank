#!/usr/bin/env bash
set -e
set -x

./gradlew clean bootJar -x test

cp build/libs/Bank*exec.jar /tmp/service.jar

open http://localhost:8080/swagger-ui/index.html
pushd /tmp
exec java -jar service.jar

