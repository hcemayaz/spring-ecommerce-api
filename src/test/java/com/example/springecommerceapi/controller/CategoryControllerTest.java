package com.example.springecommerceapi.controller;

import com.example.springecommerceapi.dto.CategoryRequest;
import com.example.springecommerceapi.dto.CategoryResponse;
import com.example.springecommerceapi.exception.NotFoundException;
import com.example.springecommerceapi.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/categories - should create category")
    void create_ShouldReturn201() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        CategoryResponse response = CategoryResponse.builder()
                .id(1L).name("Electronics").build();

        when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - should return category")
    void getById_ShouldReturn200() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .id(1L).name("Electronics").build();

        when(categoryService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/categories - should return all categories")
    void getAll_ShouldReturn200() throws Exception {
        List<CategoryResponse> responses = List.of(
                CategoryResponse.builder().id(1L).name("Electronics").build(),
                CategoryResponse.builder().id(2L).name("Books").build()
        );

        when(categoryService.getAll()).thenReturn(responses);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - should update category")
    void update_ShouldReturn200() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Electronics");

        CategoryResponse response = CategoryResponse.builder()
                .id(1L).name("Updated Electronics").build();

        when(categoryService.update(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Electronics"));
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - should delete category")
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }
}
