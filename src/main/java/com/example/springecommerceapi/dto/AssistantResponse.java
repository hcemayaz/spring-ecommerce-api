package com.example.springecommerceapi.dto;

import java.util.List;

public record AssistantResponse(
        String answer,
        List<ProductResponse> recommendedProducts
) {
}