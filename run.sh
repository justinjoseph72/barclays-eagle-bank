#!/usr/bin/env bash

./gradlew clean bootJar -x test

cp build/libs/Bank*exec.jar /tmp/service.jar

open http://localhost:8080/swagger-ui/index.html
pushd /tmp
java -jar service.jar

