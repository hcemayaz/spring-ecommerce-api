package com.example.springecommerceapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductSearchRequest(
        String keyword,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock) {
}
