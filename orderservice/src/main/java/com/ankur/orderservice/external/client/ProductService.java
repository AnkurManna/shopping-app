package com.ankur.orderservice.external.client;

import com.ankur.orderservice.exception.CustomException;
import com.ankur.orderservice.external.response.ProductResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
configuration related this external circuitbreaker is in .properties file
 */
@CircuitBreaker(name = "external" , fallbackMethod = "fallBack")
@FeignClient(name = "product" , url="${microservices.product}")
public interface ProductService {

    @PutMapping("/reduceQuantity/{id}")
    void reduceQuantity(@PathVariable("id") long productId,
                                               @RequestParam long quantity);

    @GetMapping("/{id}")
     ResponseEntity<ProductResponse> getProductById(@PathVariable("id") long productId);

    default ResponseEntity<Void>  fallBack(Exception e)
    {
        throw new CustomException("Product Service is not accessible","UNAVAILABLE",500);
    }
}
