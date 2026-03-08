package com.example.springecommerceapi.dto;

public record AssistantRequest(
        String message,
        Long userId,
        String email,
        String source
) {
}