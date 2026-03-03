package com.pollinate.ordermanagement.service;

import com.pollinate.ordermanagement.dto.ProductRequest;
import com.pollinate.ordermanagement.entity.Product;
import com.pollinate.ordermanagement.exception.ResourceNotFoundException;
import com.pollinate.ordermanagement.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;

    @Test
    void createProduct_shouldSaveAndReturn() {
        var request = ProductRequest.builder()
                .name("MacBook Pro")
                .description("16 inch laptop")
                .price(new BigDecimal("2499.99"))
                .build();

        var saved = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .description("16 inch laptop")
                .price(new BigDecimal("2499.99"))
                .build();

        when(productRepository.save(any())).thenReturn(saved);

        var result = productService.createProduct(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("MacBook Pro");
        assertThat(result.getPrice()).isEqualByComparingTo("2499.99");
    }

    @Test
    void getProductById_shouldReturnProduct() {
        var laptop = Product.builder()
                .id(5L)
                .name("Dell XPS")
                .price(new BigDecimal("1299.00"))
                .build();

        when(productRepository.findById(5L)).thenReturn(Optional.of(laptop));

        var result = productService.getProductById(5L);

        assertThat(result.getName()).isEqualTo("Dell XPS");
    }

    @Test
    void getProductById_shouldThrowWhenNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllProducts_shouldReturnList() {
        var products = List.of(
                Product.builder().id(1L).name("Product A").price(BigDecimal.TEN).build(),
                Product.builder().id(2L).name("Product B").price(BigDecimal.ONE).build()
        );
        when(productRepository.findAll()).thenReturn(products);

        var result = productService.getAllProducts();

        assertThat(result).hasSize(2);
    }
}
