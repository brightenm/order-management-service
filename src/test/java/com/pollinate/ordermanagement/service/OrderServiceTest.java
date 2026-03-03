package com.pollinate.ordermanagement.service;

import com.pollinate.ordermanagement.dto.OrderItemRequest;
import com.pollinate.ordermanagement.dto.OrderRequest;
import com.pollinate.ordermanagement.entity.Order;
import com.pollinate.ordermanagement.entity.OrderItem;
import com.pollinate.ordermanagement.entity.OrderStatus;
import com.pollinate.ordermanagement.entity.Product;
import com.pollinate.ordermanagement.exception.InvalidOrderException;
import com.pollinate.ordermanagement.exception.ResourceNotFoundException;
import com.pollinate.ordermanagement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    ProductService productService;

    @InjectMocks
    OrderService orderService;

    Product iPhone, airpods;

    @BeforeEach
    void setup() {
        iPhone = Product.builder().id(1L).name("iPhone 15").price(new BigDecimal("999.00")).build();
        airpods = Product.builder().id(2L).name("AirPods Pro").price(new BigDecimal("249.00")).build();
    }

    @Test
    void createOrder_shouldCalculateTotalCorrectly() {
        var request = OrderRequest.builder()
                .customerName("John Smith")
                .items(List.of(
                        OrderItemRequest.builder().productId(1L).quantity(1).build(),
                        OrderItemRequest.builder().productId(2L).quantity(2).build()
                ))
                .build();

        when(productService.getProductEntitiesByIds(any())).thenReturn(List.of(iPhone, airpods));

        var savedOrder = Order.builder()
                .id(100L)
                .customerName("John Smith")
                .totalPrice(new BigDecimal("1497.00")) // 999 + 249*2
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();
        
        // add items to saved order for the response mapping
        var item1 = OrderItem.builder().product(iPhone).quantity(1).unitPrice(iPhone.getPrice()).subtotal(new BigDecimal("999.00")).build();
        var item2 = OrderItem.builder().product(airpods).quantity(2).unitPrice(airpods.getPrice()).subtotal(new BigDecimal("498.00")).build();
        item1.setOrder(savedOrder);
        item2.setOrder(savedOrder);
        savedOrder.getItems().add(item1);
        savedOrder.getItems().add(item2);

        when(orderRepository.save(any())).thenReturn(savedOrder);

        var result = orderService.createOrder(request);

        assertThat(result.getTotalPrice()).isEqualByComparingTo("1497.00");
        assertThat(result.getItems()).hasSize(2);
    }

    @Test
    void createOrder_shouldRejectMissingProducts() {
        var request = OrderRequest.builder()
                .customerName("Jane Doe")
                .items(List.of(
                        OrderItemRequest.builder().productId(1L).quantity(1).build(),
                        OrderItemRequest.builder().productId(999L).quantity(1).build() // doesn't exist
                ))
                .build();

        when(productService.getProductEntitiesByIds(any())).thenReturn(List.of(iPhone)); // only iPhone found

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InvalidOrderException.class)
                .satisfies(ex -> {
                    var invalid = (InvalidOrderException) ex;
                    assertThat(invalid.getMissingProductIds()).contains(999L);
                });

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        var order = Order.builder()
                .id(1L)
                .customerName("Test User")
                .totalPrice(BigDecimal.TEN)
                .items(new ArrayList<>())
                .build();
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = orderService.getOrderById(1L);
        
        assertThat(result.getCustomerName()).isEqualTo("Test User");
    }

    @Test
    void getOrderById_shouldThrowWhenNotFound() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
