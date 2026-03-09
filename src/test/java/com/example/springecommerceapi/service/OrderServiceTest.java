package com.example.springecommerceapi.service;

import com.example.springecommerceapi.domain.*;
import com.example.springecommerceapi.dto.*;
import com.example.springecommerceapi.exception.NotFoundException;
import com.example.springecommerceapi.repository.CustomerRepository;
import com.example.springecommerceapi.repository.OrderRepository;
import com.example.springecommerceapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
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

        OrderItem orderItem = OrderItem.builder()
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

        orderRequest = OrderRequest.builder()
                .customerId(1L)
                .items(List.of(OrderItemRequest.builder()
                        .productId(100L)
                        .quantity(2)
                        .build()))
                .build();
    }

    @Nested
    @DisplayName("Create operations")
    class CreateTests {

        @Test
        @DisplayName("Should return order response when request is valid")
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
            verify(customerRepository).findById(1L);
            verify(productRepository).findById(100L);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should create order with multiple items and calculate total")
        void create_WhenMultipleItems_ShouldCalculateTotalCorrectly() {
            Product product2 = Product.builder()
                    .id(200L).name("Another Product")
                    .price(BigDecimal.valueOf(30.0)).build();

            OrderRequest multiItemRequest = OrderRequest.builder()
                    .customerId(1L)
                    .items(List.of(
                            OrderItemRequest.builder().productId(100L).quantity(2).build(),
                            OrderItemRequest.builder().productId(200L).quantity(3).build()
                    ))
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .id(20L).product(product2).quantity(3)
                    .unitPrice(BigDecimal.valueOf(30.0))
                    .lineTotal(BigDecimal.valueOf(90.0)).build();

            List<OrderItem> multiItems = new ArrayList<>(order.getItems());
            multiItems.add(item2);
            Order multiOrder = Order.builder()
                    .id(2L).customer(customer).status(OrderStatus.PENDING)
                    .totalAmount(BigDecimal.valueOf(190.0)).items(multiItems)
                    .createdAt(LocalDateTime.now()).build();

            when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(productRepository.findById(100L)).thenReturn(Optional.of(product));
            when(productRepository.findById(200L)).thenReturn(Optional.of(product2));
            when(orderRepository.save(any(Order.class))).thenReturn(multiOrder);

            OrderResponse response = orderService.create(multiItemRequest);

            assertThat(response.getItems()).hasSize(2);
            assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(190.0));
        }

        @Test
        @DisplayName("Should throw NotFoundException when product not found")
        void create_WhenProductNotFound_ShouldThrowNotFoundException() {
            OrderRequest badRequest = OrderRequest.builder()
                    .customerId(1L)
                    .items(List.of(OrderItemRequest.builder().productId(999L).quantity(1).build()))
                    .build();

            when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.create(badRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Product not found with id: 999");
        }

        @Test
        @DisplayName("Should throw NotFoundException when customer not found")
        void create_WhenCustomerNotFound_ShouldThrowNotFoundException() {
            when(customerRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.create(orderRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Customer not found with id: 1");

            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Read operations")
    class ReadTests {

        @Test
        @DisplayName("Should return order when it exists")
        void getById_WhenOrderExists_ShouldReturnOrderResponse() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            OrderResponse response = orderService.getById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getCustomerName()).isEqualTo("John Doe");
            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when order does not exist")
        void getById_WhenNotFound_ShouldThrow() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getById(99L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Order not found with id: 99");
        }

        @Test
        @DisplayName("Should return all orders")
        void getAll_ShouldReturnAllOrders() {
            when(orderRepository.findAll()).thenReturn(List.of(order));

            List<OrderResponse> responses = orderService.getAll();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getCustomerName()).isEqualTo("John Doe");
            verify(orderRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Update operations")
    class UpdateTests {

        @Test
        @DisplayName("Should return updated response when order exists")
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
            verify(orderRepository).findById(1L);
            verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Update edge cases")
    class UpdateEdgeCaseTests {

        @Test
        @DisplayName("Should throw NotFoundException when updating status of non-existent order")
        void updateStatus_WhenNotFound_ShouldThrow() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateStatus(99L, OrderStatus.SHIPPED))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Order not found with id: 99");
        }
    }

    @Nested
    @DisplayName("Delete operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete order when it exists")
        void delete_WhenOrderExists_ShouldDeleteOrder() {
            when(orderRepository.existsById(1L)).thenReturn(true);

            orderService.delete(1L);

            verify(orderRepository).existsById(1L);
            verify(orderRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleting non-existent order")
        void delete_WhenNotFound_ShouldThrow() {
            when(orderRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> orderService.delete(99L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Order not found with id: 99");

            verify(orderRepository, never()).deleteById(anyLong());
        }
    }
}
