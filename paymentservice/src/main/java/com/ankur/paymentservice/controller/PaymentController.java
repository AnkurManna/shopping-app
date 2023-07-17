package com.ankur.paymentservice.controller;

import com.ankur.paymentservice.model.PaymentRequest;
import com.ankur.paymentservice.model.PaymentResponse;
import com.ankur.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest)
    {
        return new ResponseEntity<>(
                paymentService.doPayment(paymentRequest),
                HttpStatus.OK
        );
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getTransactionDetailsByOrderId(@PathVariable long orderId)
    {
        PaymentResponse paymentResponse = paymentService.getPaymentDetailsByOrderId(orderId);

        return new ResponseEntity<>(paymentResponse,HttpStatus.OK);
    }
}
