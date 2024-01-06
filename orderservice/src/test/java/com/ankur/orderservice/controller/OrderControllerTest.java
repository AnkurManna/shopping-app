package com.ankur.orderservice.controller;

import com.ankur.orderservice.OrderServiceConfig;
import com.ankur.orderservice.entity.Order;
import com.ankur.orderservice.external.response.PaymentResponse;
import com.ankur.orderservice.external.response.ProductResponse;
import com.ankur.orderservice.model.OrderRequest;
import com.ankur.orderservice.model.OrderResponse;
import com.ankur.orderservice.model.PaymentMode;
import com.ankur.orderservice.repository.OrderRepository;
import com.ankur.orderservice.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest({"server.port=0"})
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ContextConfiguration(classes = {OrderServiceConfig.class})
public class OrderControllerTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    @RegisterExtension
    static WireMockExtension wireMockServer =
            WireMockExtension.newInstance()
                    .options(WireMockConfiguration
                            .wireMockConfig()
                            .port(8080))
                    .build();

    private ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);

    @BeforeEach
    void setup() throws IOException {

        //register the behaviour when any endpoint is called with stubbing
        getProductDetailsResponse();
        doPayment();
        getPaymentDetails();
        reduceQuantity();
    }

    private void reduceQuantity() throws IOException {

        wireMockServer.stubFor(WireMock.put(WireMock.urlMatching("/product/reduceQuantity/.*")).willReturn(
                WireMock.aResponse().withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE)
        ));

    }

    private void getPaymentDetails() throws IOException {

        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/payment/.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBody(StreamUtils.copyToString(
                                OrderControllerTest.class.getClassLoader().getResourceAsStream("/GetPayment.json"),Charset.defaultCharset()
                        ))));
    }

    private void doPayment() {

        wireMockServer.stubFor(WireMock.post("/payment")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE))
        );

    }

    private void getProductDetailsResponse() throws IOException {
        wireMockServer.stubFor(WireMock.get("/product/1")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(StreamUtils.copyToString(
                                OrderControllerTest.class.getClassLoader().getResourceAsStream("GetProduct.json"), Charset.defaultCharset()
                        ))));
    }

    @Test
    public void test_WhenPlaceOrder_DoPayment_Success() throws Exception {
        OrderRequest orderRequest = getMockOrderRequest();
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/order/placeOrder")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("Customer")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(orderRequest))

        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String orderId = result.getResponse().getContentAsString();

        Optional<Order> order = orderRepository.findById(Long.valueOf(orderId));
        assertTrue(order.isPresent());

        Order o = order.get();
        assertEquals(Long.parseLong(orderId),o.getId());
        assertEquals("PLACED",o.getOrderStatus());
        assertEquals(orderRequest.getTotalAmount(),o.getAmount());
        assertEquals(orderRequest.getQuantity(),o.getQuantity());
    }

    @Test
    public void test_WhenPlaceOrderWithWrongAccess_thenThrow403() throws Exception {
        OrderRequest orderRequest = getMockOrderRequest();
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/order/placeOrder")
                                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("Admin")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(orderRequest))

                ).andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    public void test_WhenGetOrder_Success() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/order/1")
                                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("Admin")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)

                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String actualResponse = result.getResponse().getContentAsString();
        Order order = orderRepository.findById(1l).get();
        String expectedResponse = getOrderResponse(order);

        assertEquals(expectedResponse,actualResponse);

    }

    public void test_WhenGetOrder_Order_Not_Found() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/order/2")
                                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new SimpleGrantedAuthority("Admin")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)

                ).andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    private String getOrderResponse(Order order) throws IOException {
        PaymentResponse paymentResponse = objectMapper.readValue(StreamUtils.copyToString(
                OrderControllerTest.class.getClassLoader()
                        .getResourceAsStream("GetPayment.json"),Charset.defaultCharset()
        ), PaymentResponse.class);

        paymentResponse.setPaymentStatus("SUCCESS");
        ProductResponse productResponse =
                objectMapper.readValue(StreamUtils.copyToString(
                        OrderControllerTest.class.getClassLoader()
                                .getResourceAsStream("GetProduct.json"),Charset.defaultCharset()
                ), ProductResponse.class);

        OrderResponse response = OrderResponse.builder()
                .payment(paymentResponse)
                .product(productResponse)
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .build();
        return objectMapper.writeValueAsString(response);
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .paymentMode(PaymentMode.CASH)
                .quantity(10)
                .totalAmount(200)
                .build();
    }
}