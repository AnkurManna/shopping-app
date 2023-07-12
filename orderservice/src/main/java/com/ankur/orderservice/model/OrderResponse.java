package com.ankur.orderservice.model;

import com.ankur.orderservice.external.response.PaymentResponse;
import com.ankur.orderservice.external.response.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private long orderId;
    private Instant orderDate;
    private String orderStatus;
    private long amount;

    private ProductResponse product;
    private PaymentResponse payment;


}
