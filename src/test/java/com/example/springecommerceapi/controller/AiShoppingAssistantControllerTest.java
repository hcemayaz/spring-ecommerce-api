package com.example.springecommerceapi.controller;

import com.example.springecommerceapi.dto.AssistantRequest;
import com.example.springecommerceapi.dto.AssistantResponse;
import com.example.springecommerceapi.service.AiShoppingAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiShoppingAssistantController.class)
@DisplayName("AiShoppingAssistantController Tests")
class AiShoppingAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiShoppingAssistantService assistantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/assistant/chat - should return assistant response")
    void chat_ShouldReturn200() throws Exception {
        AssistantRequest request = new AssistantRequest("laptop öner", 1L, "test@test.com", "web");
        AssistantResponse response = new AssistantResponse("İşte önerim", Collections.emptyList());

        when(assistantService.chat("laptop öner", 1L, "test@test.com", "web")).thenReturn(response);

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("İşte önerim"))
                .andExpect(jsonPath("$.recommendedProducts").isEmpty());
    }
}
