#!/bin/bash

pgrep -f "spring-boot:run" | while read -r pid; do kill "$pid" ; done
#docker stop localzipkin
#docker stop localredis
podman stop localzipkin
podman stop localredis