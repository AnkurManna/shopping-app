package com.ankur.orderservice.service;

import com.ankur.orderservice.entity.Order;
import com.ankur.orderservice.exception.CustomException;
import com.ankur.orderservice.external.client.PaymentService;
import com.ankur.orderservice.external.client.ProductService;
import com.ankur.orderservice.external.request.PaymentRequest;
import com.ankur.orderservice.external.response.PaymentResponse;
import com.ankur.orderservice.external.response.ProductResponse;
import com.ankur.orderservice.model.OrderRequest;
import com.ankur.orderservice.model.OrderResponse;
import com.ankur.orderservice.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservices.product}")
    private String productServiceUrl;
    @Value("${microservices.payment}")
    private String paymentServiceUrl;
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

        log.info("Calling Payment Service to complete payment");
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();
        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully . Changing Order status to Placed");
            orderStatus = "PLACED";
        }
        catch (Exception e)
        {
            log.error("Error occured in payment. Changing order status yo PENDING");
            orderStatus = "PAYMENT_FAILED";
        }
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order Places successfully with Order id: {}",order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for Order Id : {}",orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found the given id","NOT_FOUND",404));
        /*
        ProductResponse productResponse = productService.getProductById(order.getProductId())
                .getBody();
        PaymentResponse paymentResponse = paymentService.getPaymentDetailsByOrderId(orderId)
        .getBody();
        */
        log.info("Invoking Product Service to fetch the product id : {}",order.getProductId());
        ProductResponse productResponse = restTemplate.getForObject(
                productServiceUrl + order.getProductId(),ProductResponse.class
        );

        log.info("Invoking Payment Service to fetch the product id : {}",order.getProductId());

        PaymentResponse paymentResponse = restTemplate.getForObject(
                paymentServiceUrl + order.getId() ,PaymentResponse.class
        );
        assert paymentResponse != null;
        log.info("Payment Response is : {}",paymentResponse.getPaymentStatus());
        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .product(productResponse)
                .payment(paymentResponse)
                .build();
        return orderResponse;
    }
}
