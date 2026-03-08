package com.example.springecommerceapi.controller;

import com.example.springecommerceapi.dto.ProductRequest;
import com.example.springecommerceapi.dto.ProductResponse;
import com.example.springecommerceapi.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequest createRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("iPhone 15");
        request.setSku("IPHONE-15");
        request.setPrice(BigDecimal.valueOf(49999.90));
        request.setStockQuantity(10);
        request.setActive(true);
        request.setCategoryId(1L);
        return request;
    }

    private ProductResponse createResponse() {
        return ProductResponse.builder()
                .id(1L).name("iPhone 15").sku("IPHONE-15")
                .price(BigDecimal.valueOf(49999.90)).stockQuantity(10)
                .active(true).categoryId(1L).categoryName("Electronics")
                .build();
    }

    @Test
    @DisplayName("POST /api/products - should create product")
    void create_ShouldReturn201() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("iPhone 15"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - should return product")
    void getById_ShouldReturn200() throws Exception {
        when(productService.getById(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("IPHONE-15"));
    }

    @Test
    @DisplayName("GET /api/products - should return all products")
    void getAll_ShouldReturn200() throws Exception {
        when(productService.getAll()).thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - should update product")
    void update_ShouldReturn200() throws Exception {
        when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(createResponse());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - should delete product")
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }
}
