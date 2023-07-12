package com.ankur.orderservice.external.client;

import com.ankur.orderservice.external.request.PaymentRequest;
import com.ankur.orderservice.external.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {

    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    @GetMapping()
     ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(long orderId);


}