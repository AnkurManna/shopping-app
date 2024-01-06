#!/bin/bash

cd service-registry/

docker build -t akxmanna/serviceregistry:0.0.1 .

cd ..

cd configserver

docker build -t akxmanna/configserver:0.0.1 -t akxmanna/configserver:latest .

cd ..

cd cloudgateway

docker build -t akxmanna/cloudgateway:latest .

cd ..

cd orderservice

docker build -t akxmanna/orderservice:latest .

cd ..

cd paymentservice

docker build -t akxmanna/paymentservice:latest .

cd ..

cd productservice

docker build -t akxmanna/productservice:latest .

cd ..