package com.pollinate.ordermanagement.service;

import com.pollinate.ordermanagement.dto.OrderItemRequest;
import com.pollinate.ordermanagement.dto.OrderRequest;
import com.pollinate.ordermanagement.dto.OrderResponse;
import com.pollinate.ordermanagement.entity.Order;
import com.pollinate.ordermanagement.entity.OrderItem;
import com.pollinate.ordermanagement.entity.Product;
import com.pollinate.ordermanagement.exception.InvalidOrderException;
import com.pollinate.ordermanagement.exception.ResourceNotFoundException;
import com.pollinate.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for: {}", request.getCustomerName());

        var productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .toList();

        var products = productService.getProductEntitiesByIds(productIds);
        
        // check if all requested products exist
        var foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        var missing = productIds.stream()
                .filter(id -> !foundIds.contains(id))
                .distinct()
                .toList();

        if (!missing.isEmpty()) {
            throw new InvalidOrderException("Invalid product IDs: " + missing, missing);
        }

        var productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        var order = Order.builder()
                .customerName(request.getCustomerName())
                .items(new ArrayList<>())
                .build();

        var total = BigDecimal.ZERO;
        for (var item : request.getItems()) {
            var product = productMap.get(item.getProductId());
            var subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            
            var orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            
            order.addItem(orderItem);
            total = total.add(subtotal);
        }
        order.setTotalPrice(total);

        var saved = orderRepository.save(order);
        log.info("Order {} created, total: {}", saved.getId(), total);

        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return OrderResponse.fromEntity(order);
    }

    // TODO: add pagination for large datasets
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }
}
