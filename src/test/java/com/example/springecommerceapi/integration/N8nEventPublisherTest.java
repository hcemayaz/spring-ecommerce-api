package com.example.springecommerceapi.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("N8nEventPublisher Tests")
class N8nEventPublisherTest {

    @Test
    @DisplayName("Should skip publish when disabled")
    void publish_WhenDisabled_ShouldSkip() {
        N8nEventPublisher publisher = new N8nEventPublisher(WebClient.builder());
        ReflectionTestUtils.setField(publisher, "enabled", false);
        ReflectionTestUtils.setField(publisher, "webhookUrl", "http://localhost:5678/webhook");

        publisher.publishConversationCompletedEvent(1L, "test@test.com", "msg", "answer", Collections.emptyList(), "web");
    }

    @Test
    @DisplayName("Should skip publish when webhook URL is blank")
    void publish_WhenWebhookUrlBlank_ShouldSkip() {
        N8nEventPublisher publisher = new N8nEventPublisher(WebClient.builder());
        ReflectionTestUtils.setField(publisher, "enabled", true);
        ReflectionTestUtils.setField(publisher, "webhookUrl", "");

        publisher.publishConversationCompletedEvent(1L, "test@test.com", "msg", "answer", Collections.emptyList(), "web");
    }

    @Test
    @DisplayName("Should skip publish when webhook URL is null")
    void publish_WhenWebhookUrlNull_ShouldSkip() {
        N8nEventPublisher publisher = new N8nEventPublisher(WebClient.builder());
        ReflectionTestUtils.setField(publisher, "enabled", true);
        ReflectionTestUtils.setField(publisher, "webhookUrl", null);

        publisher.publishConversationCompletedEvent(1L, "test@test.com", "msg", "answer", Collections.emptyList(), "web");
    }
}
