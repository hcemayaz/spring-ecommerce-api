package com.example.springecommerceapi.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should publish event when enabled and webhook URL is set")
    void publish_WhenEnabledAndUrlSet_ShouldSendRequest() {
        WebClient mockWebClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(mockWebClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(mock(WebClient.RequestHeadersSpec.class));
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        WebClient.Builder mockBuilder = mock(WebClient.Builder.class);
        when(mockBuilder.defaultHeader(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockWebClient);

        N8nEventPublisher publisher = new N8nEventPublisher(mockBuilder);
        ReflectionTestUtils.setField(publisher, "enabled", true);
        ReflectionTestUtils.setField(publisher, "webhookUrl", "http://localhost:5678/webhook");
        ReflectionTestUtils.setField(publisher, "apiKey", "");

        publisher.publishConversationCompletedEvent(1L, "test@test.com", "msg", "answer", Collections.emptyList(), "web");

        verify(mockWebClient).post();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should add API key header when apiKey is present")
    void publish_WhenApiKeyPresent_ShouldAddHeader() {
        WebClient mockWebClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(mockWebClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        WebClient.Builder mockBuilder = mock(WebClient.Builder.class);
        when(mockBuilder.defaultHeader(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockWebClient);

        N8nEventPublisher publisher = new N8nEventPublisher(mockBuilder);
        ReflectionTestUtils.setField(publisher, "enabled", true);
        ReflectionTestUtils.setField(publisher, "webhookUrl", "http://localhost:5678/webhook");
        ReflectionTestUtils.setField(publisher, "apiKey", "my-secret-key");

        publisher.publishConversationCompletedEvent(1L, "test@test.com", "msg", "answer", Collections.emptyList(), "web");

        verify(bodySpec).header("X-N8N-API-KEY", "my-secret-key");
    }
}
