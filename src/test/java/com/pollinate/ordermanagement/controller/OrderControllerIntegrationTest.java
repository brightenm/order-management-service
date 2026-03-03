package com.pollinate.ordermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollinate.ordermanagement.dto.OrderItemRequest;
import com.pollinate.ordermanagement.dto.OrderRequest;
import com.pollinate.ordermanagement.entity.Product;
import com.pollinate.ordermanagement.repository.OrderRepository;
import com.pollinate.ordermanagement.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired OrderRepository orderRepository;
    @Autowired ProductRepository productRepository;

    Product laptop, mouse;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        productRepository.deleteAll();

        laptop = productRepository.save(
                Product.builder().name("Laptop").price(new BigDecimal("1200.00")).build()
        );
        mouse = productRepository.save(
                Product.builder().name("Mouse").price(new BigDecimal("50.00")).build()
        );
    }

    @Test
    @WithMockUser
    void createOrder() throws Exception {
        var request = OrderRequest.builder()
                .customerName("Alice")
                .items(List.of(
                        OrderItemRequest.builder().productId(laptop.getId()).quantity(1).build(),
                        OrderItemRequest.builder().productId(mouse.getId()).quantity(2).build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.totalPrice").value(1300.00))  // 1200 + 50*2
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    @WithMockUser
    void createOrder_rejectsMissingProduct() throws Exception {
        var request = OrderRequest.builder()
                .customerName("Bob")
                .items(List.of(
                        OrderItemRequest.builder().productId(laptop.getId()).quantity(1).build(),
                        OrderItemRequest.builder().productId(99999L).quantity(1).build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.missingProductIds", contains(99999)));
    }

    @Test
    @WithMockUser
    void createOrder_rejectsEmptyItems() throws Exception {
        var request = OrderRequest.builder()
                .customerName("Charlie")
                .items(List.of())
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_unauthorized() throws Exception {
        var request = OrderRequest.builder()
                .customerName("Test")
                .items(List.of(OrderItemRequest.builder().productId(1L).quantity(1).build()))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getOrder_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/9999"))
                .andExpect(status().isNotFound());
    }
}
