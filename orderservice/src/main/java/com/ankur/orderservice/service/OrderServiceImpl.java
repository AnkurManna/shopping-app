package com.ankur.orderservice.service;

import com.ankur.orderservice.entity.Order;
import com.ankur.orderservice.external.client.ProductService;
import com.ankur.orderservice.model.OrderRequest;
import com.ankur.orderservice.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;
    @Override
    public long placeOrder(OrderRequest orderRequest) {

        /*
         Order Service -> save the data with status order created
         Product Service -> reduce  the quantity of the ordered product
         Payment Service -> do Payments and update payment status
         */

        log.info("Placing order request: {}",orderRequest);
        productService.reduceQuantity(orderRequest.getProductId(),orderRequest.getQuantity());

        log.info("Creating Order with Status Created");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();
        order = orderRepository.save(order);
        log.info("Order Places successfully with Order id: {}",order.getId());
        return order.getId();
    }
}
