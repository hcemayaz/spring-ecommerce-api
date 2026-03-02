package com.example.springecommerceapi.service;

import com.example.springecommerceapi.dto.AssistantResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiShoppingAssistantService {

    private final ChatClient chatClient;

    public AiShoppingAssistantService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(
                        "Sen, 'Spring E-Commerce' mağazasının akıllı, yardımsever ve doğal dilde tavsiyeler verebilen alışveriş asistanısın. Kullanıcı senden bir şey istediğinde 'productSearchFunction' aracını (tool) kullanarak kataloğu tara ve sonuçlara göre en uygun ürünleri öner. Asla veritabanında olmayan hayali bir ürün önerme. Sonuçları her zaman yapılandırılmış JSON formatında dönmelisin. Parametrelerdeki kelimeleri ararken mutlaka eşanlamlı veya genel terimleri kullan (örneğin 'kablolu kulaklık' veriliyorsa sadece 'kulaklık' diye arama, uygun fiyatsa fiyat bandı olarak filtre ver).")
                .build();
    }

    public AssistantResponse chat(String userMessage) {
        log.info("Received chat message from user: {}", userMessage);

        return chatClient.prompt()
                .user(userMessage)
                .functions("productSearchFunction")
                .call()
                .entity(AssistantResponse.class);
    }
}
