#!/bin/sh
cd ../..
mvn flyway:migrate -Dflyway.configFiles=dbroad/flyway-dev/flyway-localhost.conf
