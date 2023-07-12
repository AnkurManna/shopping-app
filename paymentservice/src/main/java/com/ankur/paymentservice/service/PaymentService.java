package com.ankur.paymentservice.service;

import com.ankur.paymentservice.entity.TransactionDetails;
import com.ankur.paymentservice.model.PaymentRequest;
import com.ankur.paymentservice.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(long orderId);
}
