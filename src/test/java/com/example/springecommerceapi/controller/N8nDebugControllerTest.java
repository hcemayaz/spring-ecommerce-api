package com.example.springecommerceapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("N8nDebugController Tests")
class N8nDebugControllerTest {

    private final N8nDebugController controller = new N8nDebugController();

    @Test
    @DisplayName("Should return debug info with all fields populated")
    void debugN8n_ShouldReturnConfigStatus() {
        ReflectionTestUtils.setField(controller, "webhookUrl", "http://localhost:5678/webhook");
        ReflectionTestUtils.setField(controller, "apiKey", "secret-key");
        ReflectionTestUtils.setField(controller, "enabled", true);

        Map<String, Object> result = controller.debugN8n();

        assertThat(result.get("webhookUrl")).isEqualTo("http://localhost:5678/webhook");
        assertThat(result.get("apiKeyPresent")).isEqualTo(true);
        assertThat(result.get("enabled")).isEqualTo(true);
    }

    @Test
    @DisplayName("Should return apiKeyPresent as false when key is blank")
    void debugN8n_WhenApiKeyBlank_ShouldReturnFalse() {
        ReflectionTestUtils.setField(controller, "webhookUrl", "http://localhost:5678/webhook");
        ReflectionTestUtils.setField(controller, "apiKey", "");
        ReflectionTestUtils.setField(controller, "enabled", false);

        Map<String, Object> result = controller.debugN8n();

        assertThat(result.get("apiKeyPresent")).isEqualTo(false);
        assertThat(result.get("enabled")).isEqualTo(false);
    }

    @Test
    @DisplayName("Should return apiKeyPresent as false when key is null")
    void debugN8n_WhenApiKeyNull_ShouldReturnFalse() {
        ReflectionTestUtils.setField(controller, "webhookUrl", "");
        ReflectionTestUtils.setField(controller, "apiKey", null);
        ReflectionTestUtils.setField(controller, "enabled", true);

        Map<String, Object> result = controller.debugN8n();

        assertThat(result.get("apiKeyPresent")).isEqualTo(false);
    }
}
