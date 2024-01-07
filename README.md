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

### Config service
[Spring Cloud Config](http://cloud.spring.io/spring-cloud-config/spring-cloud-config.html) is horizontally scalable centralized configuration service for the distributed systems. It uses a pluggable repository layer that currently supports local storage, Git, and Subversion.

### Auth Server

Authorization responsibilities are extracted to a separate server, which grants [OAuth2 tokens](https://tools.ietf.org/html/rfc6749) for the backend resource services. Auth Server is used for user authorization as well as for secure machine-to-machine communication inside the perimeter.
[Okta](https://developer.okta.com/) has been used as Auth Server and implement OAuth2.0 in this app.

In this project, I use [`Client Credentials`](https://tools.ietf.org/html/rfc6749#section-4.4) grant for service-to-service communciation.

Spring Cloud Security provides convenient annotations and autoconfiguration to make this really easy to implement on both server and client side.

On the client side, everything works exactly the same as with traditional session-based authorization. You can retrieve `Principal` object from the request, check user roles using the expression-based access control and `@PreAuthorize` annotation.

``` java
    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable long orderId)
    {
        OrderResponse orderResponse = orderService.getOrderDetails(orderId);
        log.info("Order Details is : {}",orderResponse.toString());
        return new ResponseEntity<>(orderResponse,HttpStatus.OK);
    }
```

Create your account and register your application to populate `Okta configs` used in app. Common template used in different services is given below
```yml
okta:
  oauth2:
    issuer: ${okta_issuer}
    audience: api://default
    client-id: ${okta_client_id}
    client-secret: ${okta_client_secret}
    scopes: openid,profile,email,offline_access
```

### API Gateway
API Gateway is a single entry point into the system, used to handle requests and routing them to the appropriate backend service. Also, it can be used for authentication, insights, stress and canary testing, service migration, static response handling and active traffic management.

```yml
  cloud:
    gateway:
      routes:
        - id : ORDER-SERVICE
          uri: http://order-service-svc
          predicates:
            - Path=/order/**
          filters:
            - name: CircuitBreaker
              args:
                name: ORDER-SERVICE
                fallbackuri: forward:/orderServiceFallBack
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 1
                redis-rate-limiter.burstCapacity: 1

```
Above snippets says how requests starting with `/orders` will be routed to `order-service-svc` which is a `k8s Service` acting as entrypoint to `order-service` instances.
In development env we can have `lb://ORDER-SERVICE` instead of `k8s Service` for ease.

```java
    @GetMapping("/login")
    public ResponseEntity<AuthenticationResponse> login (
            @AuthenticationPrincipal OidcUser oidcUser , Model model ,
            @RegisteredOAuth2AuthorizedClient("okta") OAuth2AuthorizedClient client)
    {
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .userId(oidcUser.getEmail())
                    .accessToken(client.getAccessToken().getTokenValue())
                    .refreshToken(client.getRefreshToken().getTokenValue())
                    .expiresAt(client.getAccessToken().getExpiresAt().getEpochSecond())
                    .authorityList(oidcUser.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()))
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
    }

```
Above snippet is for login functionality getting executed at API Gateway with connection to `Okta`.

### Service Discovery

`Dev Env`

Service Discovery allows automatic detection of the network locations for all registered services. These locations might have dynamically assigned addresses due to auto-scaling, failures or upgrades.

The key part of Service discovery is the Registry. In this project, we use Netflix Eureka. Eureka is a good example of the client-side discovery pattern, where client is responsible for looking up the locations of available service instances and load balancing between them.

Client support enabled with `@EnableDiscoveryClient` annotation :
``` yml
spring:
  application:
    name: ORDER-SERVICE
```

This service will be registered with the Eureka Server and provided with metadata such as host, port, health indicator URL, home page etc. Eureka receives heartbeat messages from each instance belonging to the service. If the heartbeat fails over a configurable timetable, the instance will be removed from the registry.

Also, Eureka provides a simple interface where you can track running services and a number of available instances: `http://localhost:8761`

`Prod Env`

As the application is deployed in `K8s Cluster`, we remove eureka client and leverage `K8s Services`. Service definitions can be found in `K8s-services` branch inside `K8s` directory of each services. 


# Quickstart: 
```
$ https://github.com/AnkurManna/shopping-app.git
$ cd shopping-app

# provide config properties in properties file
# run.sh runs all the services 
$ ./run.sh
```
