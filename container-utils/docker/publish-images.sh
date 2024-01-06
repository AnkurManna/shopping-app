#!/bin/bash

export DOCKER_HUB_USER_NAME=
export DOCKER_HUB_PASSWORD=

docker login -u $DOCKER_HUB_USER_NAME -p $DOCKER_HUB_PASSWORD

docker push akxmanna/serviceregistry:latest
docker push akxmanna/configserver:latest
docker push akxmanna/cloudgateway:latest
docker push akxmanna/productservice:latest
docker push akxmanna/paymentservice:latest
docker push akxmanna/orderservice:latest