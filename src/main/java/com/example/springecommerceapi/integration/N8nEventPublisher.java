package com.example.springecommerceapi.integration;

import com.example.springecommerceapi.dto.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class N8nEventPublisher {

    private final WebClient webClient;

    @Value("${n8n.webhook-url:}")
    private String webhookUrl;

    @Value("${n8n.api-key:}")
    private String apiKey;

    @Value("${n8n.enabled:true}")
    private boolean enabled;

    public N8nEventPublisher(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void publishConversationCompletedEvent(
            Long userId,
            String email,
            String userMessage,
            String answer,
            List<ProductResponse> recommendedProducts,
            String source
    ) {
        log.info("N8nEventPublisher config => webhookUrlConfigured={}, enabled={}, apiKeyPresent={}",
                (webhookUrl != null && !webhookUrl.isBlank()), enabled, (apiKey != null && !apiKey.isBlank()));

        if (!enabled) {
            log.debug("n8n integration disabled. Skipping event publish.");
            return;
        }

        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("n8n webhook URL is not configured. Skipping event publish.");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "conversation_completed");
        payload.put("timestamp", Instant.now().toString());
        payload.put("userId", userId);
        payload.put("email", email);
        payload.put("source", source);
        payload.put("userMessage", userMessage);
        payload.put("assistantAnswer", answer);
        payload.put("recommendedProducts", recommendedProducts);

        WebClient.RequestBodySpec request = webClient.post().uri(webhookUrl);

        if (apiKey != null && !apiKey.isBlank()) {
            request = request.header("X-N8N-API-KEY", apiKey);
        }

        request
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Event sent to n8n"))
                .doOnError(error -> log.warn("Failed to send event to n8n", error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
}
