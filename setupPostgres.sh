#!/usr/bin/env bash

set +e


image_name="postgres:15.3"
cont_name="local_postgres-db"

docker stop $cont_name

sleep 2

docker rm $cont_name

docker run -d --name $cont_name -e POSTGRES_PASSWORD=postgres -v ./init.sql:/docker-entrypoint-initdb.d/init.sql -p 5432:5432 $image_name
