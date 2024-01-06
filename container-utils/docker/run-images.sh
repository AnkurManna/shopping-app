#!/bin/bash

docker run -d p8761:8761 --name serviceregistry akxmanna/serviceregistry

docker run -d p9296:9296 -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka --name configserver akxmanna/configserver:latest

docker run -d p9090:9090 -e CONFIG_SERVER_URL=http://host.docker.internal -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka --name cloudgateway akxmanna/cloudgateway:latest

docker run -d p8084:8084 -e CONFIG_SERVER_URL=http://host.docker.internal -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka --name orderservice akxmanna/orderservice:latest

docker run -d p8083:8083 -e CONFIG_SERVER_URL=http://host.docker.internal -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka --name paymentservice akxmanna/paymentservice:latest

docker run -d p8080:8080 -e CONFIG_SERVER_URL=http://host.docker.internal -e EUREKA_SERVER_ADDRESS=http://host.docker.internal:8761/eureka --name productservice akxmanna/productservice:latest
