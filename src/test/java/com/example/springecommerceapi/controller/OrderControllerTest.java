package com.example.springecommerceapi.controller;

import com.example.springecommerceapi.domain.OrderStatus;
import com.example.springecommerceapi.dto.OrderItemRequest;
import com.example.springecommerceapi.dto.OrderRequest;
import com.example.springecommerceapi.dto.OrderResponse;
import com.example.springecommerceapi.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponse createResponse() {
        return OrderResponse.builder()
                .id(1L).customerId(1L).customerName("John Doe")
                .totalAmount(BigDecimal.valueOf(100.0))
                .status(OrderStatus.PENDING)
                .items(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("POST /api/orders - should create order")
    void create_ShouldReturn201() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .customerId(1L)
                .items(List.of(OrderItemRequest.builder().productId(1L).quantity(2).build()))
                .build();

        when(orderService.create(any(OrderRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - should return order")
    void getById_ShouldReturn200() throws Exception {
        when(orderService.getById(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/orders - should return all orders")
    void getAll_ShouldReturn200() throws Exception {
        when(orderService.getAll()).thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - should update status")
    void updateStatus_ShouldReturn200() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L).customerId(1L).customerName("John Doe")
                .totalAmount(BigDecimal.valueOf(100.0))
                .status(OrderStatus.SHIPPED)
                .items(Collections.emptyList())
                .build();

        when(orderService.updateStatus(eq(1L), eq(OrderStatus.SHIPPED))).thenReturn(response);

        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "SHIPPED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - should fail when status is missing")
    void updateStatus_WhenStatusMissing_ShouldFail() throws Exception {
        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - should delete order")
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(orderService).delete(1L);

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService).delete(1L);
    }
}
