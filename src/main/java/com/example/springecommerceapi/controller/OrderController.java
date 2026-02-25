package com.example.springecommerceapi.controller;

import com.example.springecommerceapi.domain.OrderStatus;
import com.example.springecommerceapi.dto.OrderRequest;
import com.example.springecommerceapi.dto.OrderResponse;
import com.example.springecommerceapi.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid OrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderService.getAll();
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @RequestBody Map<String, String> statusUpdate) {
        String statusStr = statusUpdate.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("Status is required");
        }
        OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        return orderService.updateStatus(id, newStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }
}
