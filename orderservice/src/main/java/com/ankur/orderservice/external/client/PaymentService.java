package com.ankur.orderservice.external.client;

import com.ankur.orderservice.exception.CustomException;
import com.ankur.orderservice.external.request.PaymentRequest;
import com.ankur.orderservice.external.response.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CircuitBreaker(name = "external",fallbackMethod = "fallBack")
@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {

    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    @GetMapping()
     ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(long orderId);

    default ResponseEntity<Long> fallBack(Exception e)
    {
        throw new CustomException("Payment Service is not accessible","UNAVAILABLE",500);
    }

}