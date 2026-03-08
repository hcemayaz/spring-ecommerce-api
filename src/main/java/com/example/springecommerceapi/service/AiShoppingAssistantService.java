package com.example.springecommerceapi.service;

import com.example.springecommerceapi.dto.AssistantResponse;
import com.example.springecommerceapi.dto.ProductResponse;
import com.example.springecommerceapi.integration.N8nEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class AiShoppingAssistantService {

    private final ChatClient chatClient;
    private final N8nEventPublisher n8nEventPublisher;
    private final ObjectMapper objectMapper;

    public AiShoppingAssistantService(ChatClient.Builder chatClientBuilder,
                                      N8nEventPublisher n8nEventPublisher,
                                      ObjectMapper objectMapper) {

        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        Sen, 'Spring E-Commerce' mağazasının akıllı, yardımsever ve doğal dilde tavsiyeler verebilen alışveriş asistanısın. \
                        Kullanıcı senden bir şey istediğinde 'productSearchFunction' aracını (tool) kullanarak kataloğu tara ve \
                        sonuçlara göre en uygun ürünleri öner. Asla veritabanında olmayan hayali bir ürün önerme. \
                        Cevabını HER ZAMAN AŞAĞIDAKİ ŞEMAYA UYAN, GEÇERLİ JSON OLARAK DÖN:
                        { "answer": "kısa doğal dil açıklama", "recommendedProducts": [ { "id": 1, "name": "...", "sku": "...", "price": 199.99, "stockQuantity": 10, "active": true, "categoryId": 1, "categoryName": "...", "createdAt": "2024-01-01T10:00:00", "updatedAt": "2024-01-01T10:00:00" } ] }
                        ÖNEMLİ:
                        - JSON içinde ASLA '\\\\' karakterini para birimi veya apostrof için kullanma. Özellikle '\\\\'' (ters eğik çizgi + tek tırnak) yazma.
                        - '$' karakteri KULLANMA. Fiyatları JSON'da SADECE SAYI olarak yaz (örnek: 199.99). Para birimi ekleme.
                        - JSON içinde markdown, backtick, yeni satır kaçışları gibi karmaşık şeyler kullanma.
                        - Sadece geçerli JSON üret; Türkçe açıklamayı 'answer' alanına yaz, ürünleri 'recommendedProducts' listesine koy.\
                        """)
                .build();

        this.n8nEventPublisher = n8nEventPublisher;
        this.objectMapper = objectMapper;
    }

    public AssistantResponse chat(String userMessage) {
        return chat(userMessage, null, null, "ecommerce_chat");
    }

    public AssistantResponse chat(String userMessage,
                                  Long userId,
                                  String email,
                                  String source) {

        log.info("Received chat message from user (userId={}): {}", userId, userMessage);

        AssistantResponse assistantResponse;

        try {
            String rawContent = chatClient
                    .prompt()
                    .user(userMessage)
                    .functions("productSearchFunction")
                    .call()
                    .content();

            log.info("Raw model response: {}", rawContent);

            String cleaned = sanitizeJsonLikeString(rawContent);

            log.info("Sanitized model response: {}", cleaned);

            assistantResponse = objectMapper.readValue(cleaned, AssistantResponse.class);

            log.info("Assistant parsed successfully. answerLength={}, recommendedProducts={}",
                    assistantResponse.answer() != null ? assistantResponse.answer().length() : 0,
                    assistantResponse.recommendedProducts() != null ? assistantResponse.recommendedProducts().size() : 0
            );

        } catch (Exception ex) {
            log.error("Error while calling AI shopping assistant", ex);

            String fallbackAnswer = "Şu anda akıllı asistanımızda bir sorun oluştu. " +
                    "Lütfen daha sonra tekrar dener misin?";
            List<ProductResponse> emptyList = Collections.emptyList();

            assistantResponse = new AssistantResponse(fallbackAnswer, emptyList);
        }

        try {
            n8nEventPublisher.publishConversationCompletedEvent(
                    userId,
                    email,
                    userMessage,
                    assistantResponse.answer(),
                    assistantResponse.recommendedProducts(),
                    (source != null && !source.isBlank()) ? source : "ecommerce_chat"
            );
        } catch (Exception e) {
            log.warn("Failed to publish conversation_completed event to n8n", e);
        }

        return assistantResponse;
    }

    private String sanitizeJsonLikeString(String raw) {
        if (raw == null) {
            return "{}";
        }

        String s = raw.trim();

        int firstBrace = s.indexOf('{');
        int lastBrace = s.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            s = s.substring(firstBrace, lastBrace + 1);
        } else {
            String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
            return "{\"answer\":\"" + escaped + "\",\"recommendedProducts\":[]}";
        }

        s = s.replace("\\'", "'");

        s = s.replaceAll("\\\\([^\"\\\\bfnrtu/])", "$1");

        s = s.replaceAll("\"(\\d+(?:\\.\\d+)?)\\s*TL\"", "\"$1\"");

        return s;
    }
}