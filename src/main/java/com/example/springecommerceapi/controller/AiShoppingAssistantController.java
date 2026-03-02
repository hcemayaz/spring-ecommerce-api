package com.example.springecommerceapi.controller;

import com.example.springecommerceapi.dto.AssistantRequest;
import com.example.springecommerceapi.dto.AssistantResponse;
import com.example.springecommerceapi.service.AiShoppingAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AiShoppingAssistantController {

    private final AiShoppingAssistantService assistantService;

    @PostMapping("/chat")
    public AssistantResponse chat(@RequestBody AssistantRequest request) {
        return assistantService.chat(request.message());
    }
}
