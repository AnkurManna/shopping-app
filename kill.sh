#!/bin/bash

pgrep -f "spring-boot:run" | while read -r pid; do kill "$pid" ; done
