package com.ankur.paymentservice.service;

import com.ankur.paymentservice.model.PaymentRequest;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);
}
