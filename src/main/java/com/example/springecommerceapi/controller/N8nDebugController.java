package com.example.springecommerceapi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Profile("dev")
public class N8nDebugController {

    @Value("${n8n.webhook-url:}")
    private String webhookUrl;

    @Value("${n8n.api-key:}")
    private String apiKey;

    @Value("${n8n.enabled:true}")
    private boolean enabled;

    @GetMapping("/debug/n8n")
    public Map<String, Object> debugN8n() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("webhookUrl", webhookUrl);
        result.put("apiKeyPresent", apiKey != null && !apiKey.isBlank());
        result.put("enabled", enabled);
        return result;
    }
}