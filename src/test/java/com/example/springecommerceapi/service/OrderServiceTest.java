package com.example.springecommerceapi.service;

import com.example.springecommerceapi.domain.*;
import com.example.springecommerceapi.dto.*;
import com.example.springecommerceapi.exception.NotFoundException;
import com.example.springecommerceapi.repository.CustomerRepository;
import com.example.springecommerceapi.repository.OrderRepository;
import com.example.springecommerceapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product product;
    private Order order;
    private OrderItem orderItem;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        product = Product.builder()
                .id(100L)
                .name("Test Product")
                .price(BigDecimal.valueOf(50.0))
                .build();

        orderItem = OrderItem.builder()
                .id(10L)
                .product(product)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.0))
                .lineTotal(BigDecimal.valueOf(100.0))
                .build();

        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        order = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(100.0))
                .items(items)
                .createdAt(LocalDateTime.now())
                .build();

        orderItem.setOrder(order);

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(100L)
                .quantity(2)
                .build();

        orderRequest = OrderRequest.builder()
                .customerId(1L)
                .items(List.of(itemRequest))
                .build();
    }

    @Test
    void create_WhenValidRequest_ShouldReturnOrderResponse() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.create(orderRequest);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(100.0));

        verify(customerRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(100L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void create_WhenCustomerNotFound_ShouldThrowNotFoundException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found with id: 1");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getById_WhenOrderExists_ShouldReturnOrderResponse() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCustomerName()).isEqualTo("John Doe");
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void updateStatus_WhenOrderExists_ShouldReturnUpdatedResponse() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order updatedOrder = Order.builder()
                .id(1L)
                .customer(customer)
                .status(OrderStatus.SHIPPED)
                .totalAmount(BigDecimal.valueOf(100.0))
                .items(order.getItems())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        OrderResponse response = orderService.updateStatus(1L, OrderStatus.SHIPPED);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void delete_WhenOrderExists_ShouldDeleteOrder() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        orderService.delete(1L);

        verify(orderRepository, times(1)).existsById(1L);
        verify(orderRepository, times(1)).deleteById(1L);
    }
}
