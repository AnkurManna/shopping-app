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
import com.ankur.orderservice.model.PaymentMode;
import com.ankur.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();

    @Value("${microservices.product}")
    private String productServiceUrl;

    @Value("${microservices.payment}")
    private String paymentServiceUrl;

    @BeforeEach
    public void setup()
    {
        ReflectionTestUtils.setField(orderService,"productServiceUrl",productServiceUrl);
        ReflectionTestUtils.setField(orderService,"paymentServiceUrl",paymentServiceUrl);
    }

    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success()
    {
        Order order = getMockOrder();
        when(orderRepository.findById(anyLong()))
                        .thenReturn(Optional.of(order));
        when(restTemplate.getForObject(
                productServiceUrl + order.getProductId(), ProductResponse.class
        )).thenReturn(getMockProductResponse());

        when(restTemplate.getForObject(
                paymentServiceUrl + order.getId() , PaymentResponse.class
        )).thenReturn(getMockPaymentResponse());

        OrderResponse orderResponse = orderService.getOrderDetails(1L);

        verify(orderRepository,times(1)).findById(anyLong());
        verify(restTemplate,times(1)).getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(), ProductResponse.class);
        verify(restTemplate,times(1)).getForObject("http://PAYMENT-SERVICE/payment/" + order.getId() , PaymentResponse.class);

        assertNotNull(orderResponse);
        assertEquals(order.getId(),orderResponse.getOrderId());
    }

    @DisplayName("Get Orders - Failure Scenario")
    @Test
    void test_When_Get_Order_NOT_FOUND_then_Not_Found()
    {
        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(null));

        //OrderResponse orderResponse = orderService.getOrderDetails(1);

        CustomException exception = assertThrows(CustomException.class,()-> orderService.getOrderDetails(1));
        assertEquals("NOT_FOUND",exception.getErrorCode());
        assertEquals(404,exception.getStatus());

        verify(orderRepository,times(1)).findById(anyLong());
    }

    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success()
    {
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        doNothing().when(productService).reduceQuantity(anyLong(),anyLong());

        when(paymentService.doPayment(any(PaymentRequest.class))).thenReturn(
                new ResponseEntity<Long>(1l,HttpStatus.OK)
        );
        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository,times(2)).save(any());
        verify(productService,times(1)).reduceQuantity(anyLong(),anyLong());
        verify(paymentService,times(1)).doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(),orderId);
    }

    @DisplayName("Place Order - Payment Failed")
    @Test
    void test_Place_Order_when_Payment_Fails_then_Order_Placed()
    {
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        doNothing().when(productService).reduceQuantity(anyLong(),anyLong());

        when(paymentService.doPayment(any(PaymentRequest.class))).thenThrow(
                new RuntimeException()
        );
        long orderId = orderService.placeOrder(orderRequest);
        assertEquals(order.getId(),orderId);

    }
    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .quantity(10)
                .paymentMode(PaymentMode.CASH)
                .totalAmount(100)
                .build();
    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentDate(Instant.now())
                .paymentMode(String.valueOf(PaymentMode.CASH))
                .amount(200)
                .orderId(1)
                .paymentStatus("ACCEPTED")
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productName("iPhone")
                .productId(2)
                .price(100)
                .quantity(200)
                .build();
    }

    private Order getMockOrder()
    {
        return Order.builder()
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .id(1)
                .amount(1000)
                .quantity(200)
                .productId(2)
                .build();
    }
}