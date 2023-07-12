package com.ankur.orderservice.service;

import com.ankur.orderservice.model.OrderRequest;
import com.ankur.orderservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
