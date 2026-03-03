package com.pollinate.ordermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pollinate.ordermanagement.dto.ProductRequest;
import com.pollinate.ordermanagement.entity.Product;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProductRepository productRepository;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void createProduct() throws Exception {
        var request = new ProductRequest("Gaming Mouse", "RGB wireless", new BigDecimal("79.99"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Gaming Mouse"))
                .andExpect(jsonPath("$.price").value(79.99));
    }

    @Test
    @WithMockUser
    void createProduct_invalidInput() throws Exception {
        var request = new ProductRequest("", null, new BigDecimal("-10"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_unauthorized() throws Exception {
        var request = new ProductRequest("Test", "desc", BigDecimal.TEN);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getProduct() throws Exception {
        var saved = productRepository.save(
                Product.builder().name("Keyboard").price(new BigDecimal("149.00")).build()
        );

        mockMvc.perform(get("/api/v1/products/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    @WithMockUser
    void getProduct_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void listProducts() throws Exception {
        productRepository.save(Product.builder().name("Item 1").price(BigDecimal.TEN).build());
        productRepository.save(Product.builder().name("Item 2").price(BigDecimal.ONE).build());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
