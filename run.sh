#!/bin/bash

# Specify the directory path
serviceregistry="./service-registry/"
configserver="./configserver/"
productservice="./productservice/"
orderservice="./orderservice/"
paymentservice="./paymentservice/"

# Function to navigate to a directory, build, and run the Spring Boot project in the background
run_service() {
    local project_dir=$1
    local service=$2

    echo "Navigating to $project_dir"
    cd "$project_dir"

    echo "Building the $service"
    mvn clean install

    echo "Running the $service in the background"
    nohup mvn spring-boot:run > /dev/null 2>&1 &
    sleep 20
    cd ..
}

# Run the Spring Boot projects
run_service "$serviceregistry" "serviceregistry"
run_service "$configserver" "configserver"
run_service "$productservice" "productservice"