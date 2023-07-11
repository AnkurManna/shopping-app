package com.ankur.orderservice.service;

import com.ankur.orderservice.model.OrderRequest;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
}
