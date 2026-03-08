package com.example.springecommerceapi.service;

import com.example.springecommerceapi.dto.AssistantResponse;
import com.example.springecommerceapi.integration.N8nEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiShoppingAssistantServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private N8nEventPublisher n8nEventPublisher;

    private ObjectMapper objectMapper;

    private AiShoppingAssistantService assistantService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        assistantService = new AiShoppingAssistantService(chatClientBuilder, n8nEventPublisher, objectMapper);
    }

    @Test
    void chat_WhenValidJsonResponse_ShouldReturnParsedAssistantResponse() {
        String modelResponse = "{\"answer\": \"İşte önerim\", \"recommendedProducts\": []}";
        mockChatClientChain(modelResponse);

        AssistantResponse response = assistantService.chat("laptop öner", 1L, "test@test.com", "ecommerce_chat");

        assertThat(response.answer()).isEqualTo("İşte önerim");
        assertThat(response.recommendedProducts()).isEmpty();
    }

    @Test
    void chat_WhenValidJsonWithProducts_ShouldReturnProducts() {
        String modelResponse = "{\"answer\": \"Bu ürünü öneririm\", \"recommendedProducts\": [" +
                "{\"id\": 1, \"name\": \"Laptop\", \"sku\": \"LAP-001\", \"price\": 299.99, " +
                "\"stockQuantity\": 10, \"active\": true, \"categoryId\": 1, \"categoryName\": \"Elektronik\"}" +
                "]}";
        mockChatClientChain(modelResponse);

        AssistantResponse response = assistantService.chat("laptop öner");

        assertThat(response.answer()).isEqualTo("Bu ürünü öneririm");
        assertThat(response.recommendedProducts()).hasSize(1);
        assertThat(response.recommendedProducts().get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void chat_WhenModelThrowsException_ShouldReturnFallbackResponse() {
        mockChatClientChainThrowsException(new RuntimeException("Model error"));

        AssistantResponse response = assistantService.chat("laptop öner", 1L, "test@test.com", "ecommerce_chat");

        assertThat(response.answer()).contains("sorun oluştu");
        assertThat(response.recommendedProducts()).isEmpty();
    }

    @Test
    void chat_WhenModelReturnsInvalidJson_ShouldReturnFallbackResponse() {
        mockChatClientChain("bu geçerli bir json değil");

        AssistantResponse response = assistantService.chat("laptop öner", 1L, "test@test.com", "ecommerce_chat");

        assertThat(response.answer()).contains("sorun oluştu");
        assertThat(response.recommendedProducts()).isEmpty();
    }

    @Test
    void chat_WhenJsonWrappedWithExtraText_ShouldParseSuccessfully() {
        String modelResponse = "İşte sonuç: {\"answer\": \"Önerim budur\", \"recommendedProducts\": []} son.";
        mockChatClientChain(modelResponse);

        AssistantResponse response = assistantService.chat("telefon öner");

        assertThat(response.answer()).isEqualTo("Önerim budur");
        assertThat(response.recommendedProducts()).isEmpty();
    }

    @Test
    void chat_WhenPriceContainsTL_ShouldSanitizeAndParse() {
        String modelResponse = "{\"answer\": \"Önerim\", \"recommendedProducts\": [" +
                "{\"id\": 1, \"name\": \"Laptop\", \"sku\": \"LAP-001\", \"price\": \"299.99 TL\", " +
                "\"stockQuantity\": 5, \"active\": true, \"categoryId\": 1, \"categoryName\": \"Elektronik\"}" +
                "]}";
        mockChatClientChain(modelResponse);

        AssistantResponse response = assistantService.chat("laptop öner");

        assertThat(response.recommendedProducts()).hasSize(1);
        assertThat(response.recommendedProducts().get(0).getPrice()).isEqualByComparingTo("299.99");
    }

    @Test
    void chat_WhenJsonContainsEscapedSingleQuotes_ShouldSanitizeAndParse() {
        String modelResponse = "{\"answer\": \"Kullanıcı\\'nın önerisi\", \"recommendedProducts\": []}";
        mockChatClientChain(modelResponse);

        AssistantResponse response = assistantService.chat("öner");

        assertThat(response.answer()).isEqualTo("Kullanıcı'nın önerisi");
    }

    @Test
    void chat_ShouldPublishN8nEvent() {
        String modelResponse = "{\"answer\": \"Önerim\", \"recommendedProducts\": []}";
        mockChatClientChain(modelResponse);

        assistantService.chat("laptop öner", 1L, "test@test.com", "web");

        verify(n8nEventPublisher, times(1)).publishConversationCompletedEvent(
                eq(1L), eq("test@test.com"), eq("laptop öner"),
                eq("Önerim"), anyList(), eq("web")
        );
    }

    @Test
    void chat_WhenN8nPublisherFails_ShouldStillReturnResponse() {
        String modelResponse = "{\"answer\": \"Önerim\", \"recommendedProducts\": []}";
        mockChatClientChain(modelResponse);
        doThrow(new RuntimeException("n8n down")).when(n8nEventPublisher)
                .publishConversationCompletedEvent(any(), any(), any(), any(), any(), any());

        AssistantResponse response = assistantService.chat("laptop öner", 1L, "test@test.com", "web");

        assertThat(response.answer()).isEqualTo("Önerim");
    }

    @Test
    void chat_WhenSourceIsNull_ShouldDefaultToEcommerceChat() {
        String modelResponse = "{\"answer\": \"Önerim\", \"recommendedProducts\": []}";
        mockChatClientChain(modelResponse);

        assistantService.chat("laptop öner", 1L, "test@test.com", null);

        verify(n8nEventPublisher).publishConversationCompletedEvent(
                eq(1L), eq("test@test.com"), eq("laptop öner"),
                eq("Önerim"), anyList(), eq("ecommerce_chat")
        );
    }

    @Test
    void chat_WhenSourceIsBlank_ShouldDefaultToEcommerceChat() {
        String modelResponse = "{\"answer\": \"Önerim\", \"recommendedProducts\": []}";
        mockChatClientChain(modelResponse);

        assistantService.chat("laptop öner", 1L, "test@test.com", "   ");

        verify(n8nEventPublisher).publishConversationCompletedEvent(
                eq(1L), eq("test@test.com"), eq("laptop öner"),
                eq("Önerim"), anyList(), eq("ecommerce_chat")
        );
    }

    @Test
    void chat_SingleParamOverload_ShouldDelegateWithDefaults() {
        String modelResponse = "{\"answer\": \"Önerim\", \"recommendedProducts\": []}";
        mockChatClientChain(modelResponse);

        assistantService.chat("laptop öner");

        verify(n8nEventPublisher).publishConversationCompletedEvent(
                isNull(), isNull(), eq("laptop öner"),
                eq("Önerim"), anyList(), eq("ecommerce_chat")
        );
    }

    @Test
    void sanitizeJsonLikeString_WhenNull_ShouldReturnEmptyJson() throws Exception {
        String result = invokeSanitize(null);
        assertThat(result).isEqualTo("{}");
    }

    @Test
    void sanitizeJsonLikeString_WhenNoBraces_ShouldReturnTrimmedString() throws Exception {
        String result = invokeSanitize("no json here");
        assertThat(result).isEqualTo("no json here");
    }

    @Test
    void sanitizeJsonLikeString_WhenPriceWithTL_ShouldRemoveTL() throws Exception {
        String result = invokeSanitize("{\"price\": \"500 TL\"}");
        assertThat(result).isEqualTo("{\"price\": \"500\"}");
    }

    @Test
    void sanitizeJsonLikeString_WhenPriceWithDecimalTL_ShouldRemoveTL() throws Exception {
        String result = invokeSanitize("{\"price\": \"199.99 TL\"}");
        assertThat(result).isEqualTo("{\"price\": \"199.99\"}");
    }


    private void mockChatClientChain(String modelResponse) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.functions(any(String[].class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(modelResponse);
    }

    private void mockChatClientChainThrowsException(RuntimeException exception) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.functions(any(String[].class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenThrow(exception);
    }

    private String invokeSanitize(String input) throws Exception {
        Method method = AiShoppingAssistantService.class.getDeclaredMethod("sanitizeJsonLikeString", String.class);
        method.setAccessible(true);
        return (String) method.invoke(assistantService, input);
    }
}
