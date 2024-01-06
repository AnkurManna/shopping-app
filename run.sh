#!/bin/bash

# Specify the directory path
serviceregistry="./service-registry/"
configserver="./configserver/"
cloudgateway="./cloudgateway/"
productservice="./productservice/"
orderservice="./orderservice/"
paymentservice="./paymentservice/"

# Function to navigate to a directory, build, and run the Spring Boot project in the background
run_core_service() {
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

run_utility_service()
{
  echo "Running Utility Services "
  #docker run --name localzipkin -d -p 9411:9411 openzipkin/zipkin
  podman run --name localzipkin -dt -p 9411:9411/tcp docker.io/openzipkin/zipkin
  echo "Started zipkin container with name localzipkin"
  #docker run --name localredis -d -p 6379:6379 redis
  podman run --name localredis -dt -p 6379:6379/tcp docker.io/library/redis
  echo "Started redis container with name localredis"
}

export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.9.jdk/Contents/Home/
# Run Utility Services
#run_utility_service
# Run the Spring Boot Services
run_core_service "$serviceregistry" "serviceregistry"
run_core_service "$configserver" "configserver"
run_core_service "$cloudgateway" "cloudgateway"
run_core_service "$productservice" "productservice"
run_core_service "$orderservice" "orderservice"
run_core_service "$paymentservice" "paymentservice"
#to do : develop script which doesn't discard logs i.e. opens a new terminal and run process in that