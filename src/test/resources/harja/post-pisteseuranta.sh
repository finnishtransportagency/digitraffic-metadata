#!/usr/bin/env bash
curl -i --header "Content-Type: application/json" --request POST --data @controller/pisteseuranta.json http://localhost:9010/api/v1/maintenance/tracking/work_machine
