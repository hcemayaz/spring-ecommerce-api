package com.example.springecommerceapi.controller;

import com.example.springecommerceapi.dto.CustomerRequest;
import com.example.springecommerceapi.dto.CustomerResponse;
import com.example.springecommerceapi.service.CustomerService;
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

@WebMvcTest(CustomerController.class)
@DisplayName("CustomerController Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRequest createRequest() {
        return CustomerRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").phone("1234567890")
                .build();
    }

    private CustomerResponse createResponse() {
        return CustomerResponse.builder()
                .id(1L).firstName("John").lastName("Doe")
                .email("john@example.com").phone("1234567890")
                .build();
    }

    @Test
    @DisplayName("POST /api/customers - should create customer")
    void create_ShouldReturn201() throws Exception {
        when(customerService.create(any(CustomerRequest.class))).thenReturn(createResponse());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("GET /api/customers/{id} - should return customer")
    void getById_ShouldReturn200() throws Exception {
        when(customerService.getById(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /api/customers - should return all customers")
    void getAll_ShouldReturn200() throws Exception {
        when(customerService.getAll()).thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/customers/{id} - should update customer")
    void update_ShouldReturn200() throws Exception {
        when(customerService.update(eq(1L), any(CustomerRequest.class))).thenReturn(createResponse());

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/customers/{id} - should delete customer")
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(customerService).delete(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).delete(1L);
    }
}
