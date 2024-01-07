### Backend System for a Shopping App Implemented in microservices Architecture
This Shopping App is a simple e-commerce app(backend only) built to demonstrate the Microservice Architecture Pattern using Spring Boot, Spring Cloud and Docker. The project is intended for learning purpose, but you are welcome to fork it and turn it into something else!

## Functional Services
This app is decomposed into three core microservices. All of them are independently deployable applications organized around certain business domains.

![functional drawio](https://github.com/AnkurManna/shopping-app/assets/53156149/d321763e-f52f-4aef-b2ad-ee42f7426fc3)

#### Product service
Contains general input logic and validation: product items, name ,price and quantity.

Method	| Path	| Description	| Customer allowed	| Admin allowed
------------- | ------------------------- | ------------- |:-------------:|:----------------:|
GET	| /products/{id}	| Get Product data	|   | 	×
PUT	| /products/{id}	| Update Product data	| × | 
POST	| /products/	| Add new product	| x | 

#### Order service
Performs checks on order data, update quantity for particular product, initiate payment request, and place order.

Method	| Path	| Description	| Customer allowed	| Admin allowed
------------- | ------------------------- | ------------- |:-------------:|:----------------:|
GET	| /orders/{id}	| Get Order data	|   | 	
POST	| /orders/placeOrder	| Place new order	|  | 

#### Payment service
Performs payment and fetches transaction details

Method	| Path	| Description	| Scopes allowed	| Admin allowed
------------- | ------------------------- | ------------- |:-------------:|:----------------:|
GET	| /payment/{id}	| Get Transaction details	| internal  | 	
POST	| /payment/doPayment	| Do Payment(only function calls no gateway involved)	| internal | 

#### Notes
- Each microservice has its own database, so there is no way to bypass API and access persistence data directly.
- MySql is used as a primary database for each of the services.
- Services are talking to each other via the Rest API as per requirement
- Internal Scope allows only services to be called by services without allowing external client
## Infrastructure
[Spring cloud](https://spring.io/projects/spring-cloud) provides powerful tools for developers to quickly implement common distributed systems patterns -

![shopping-app-infra drawio](https://github.com/AnkurManna/shopping-app/assets/53156149/921da8bb-3b2a-4108-8b2f-ea96652ef32f)



![shopping-app drawio](https://github.com/AnkurManna/shopping-app/assets/53156149/8d24ee98-13c0-4950-8fa4-3e0682cc8beb)

# Quickstart: 
```
$ https://github.com/AnkurManna/shopping-app.git
$ cd shopping-app

# provide config properties in properties file
# run.sh runs all the services 
$ ./run.sh
```
