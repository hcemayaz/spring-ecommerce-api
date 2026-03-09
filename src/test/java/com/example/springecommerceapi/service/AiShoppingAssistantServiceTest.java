package com.example.springecommerceapi.service;

import com.example.springecommerceapi.dto.AssistantResponse;
import com.example.springecommerceapi.integration.N8nEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("AiShoppingAssistantService Tests")
class AiShoppingAssistantServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private N8nEventPublisher n8nEventPublisher;

    private AiShoppingAssistantService assistantService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        assistantService = new AiShoppingAssistantService(chatClientBuilder, n8nEventPublisher, objectMapper);
    }

    @Nested
    @DisplayName("Successful response parsing")
    class SuccessfulParsingTests {

        @Test
        @DisplayName("Should return parsed response for valid JSON")
        void chat_WhenValidJsonResponse_ShouldReturnParsedAssistantResponse() {
            mockChatClientChain("{\"answer\": \"İşte önerim\", \"recommendedProducts\": []}");

            AssistantResponse response = assistantService.chat("laptop öner", 1L, "test@test.com", "ecommerce_chat");

            assertThat(response.answer()).isEqualTo("İşte önerim");
            assertThat(response.recommendedProducts()).isEmpty();
        }

        @Test
        @DisplayName("Should return products for valid JSON with products")
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
        @DisplayName("Should parse JSON wrapped with extra text")
        void chat_WhenJsonWrappedWithExtraText_ShouldParseSuccessfully() {
            mockChatClientChain("İşte sonuç: {\"answer\": \"Önerim budur\", \"recommendedProducts\": []} son.");

            AssistantResponse response = assistantService.chat("telefon öner");

            assertThat(response.answer()).isEqualTo("Önerim budur");
            assertThat(response.recommendedProducts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Fallback responses")
    class FallbackTests {

        @Test
        @DisplayName("Should return fallback when model throws exception")
        void chat_WhenModelThrowsException_ShouldReturnFallbackResponse() {
            mockChatClientChainThrowsException(new RuntimeException("Model error"));

            AssistantResponse response = assistantService.chat("laptop öner", 1L, "test@test.com", "ecommerce_chat");

            assertThat(response.answer()).contains("sorun oluştu");
            assertThat(response.recommendedProducts()).isEmpty();
        }

        @Test
        @DisplayName("Should wrap non-JSON response as answer when model returns invalid JSON")
        void chat_WhenModelReturnsInvalidJson_ShouldWrapAsAnswer() {
            mockChatClientChain("bu geçerli bir json değil");

            AssistantResponse response = assistantService.chat("laptop öner", 1L, "test@test.com", "ecommerce_chat");

            assertThat(response.answer()).isEqualTo("bu geçerli bir json değil");
            assertThat(response.recommendedProducts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("JSON sanitization")
    class SanitizationTests {

        @Test
        @DisplayName("Should sanitize price containing TL")
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
        @DisplayName("Should sanitize escaped single quotes in JSON")
        void chat_WhenJsonContainsEscapedSingleQuotes_ShouldSanitizeAndParse() {
            mockChatClientChain("{\"answer\": \"Kullanıcı\\'nın önerisi\", \"recommendedProducts\": []}");

            AssistantResponse response = assistantService.chat("öner");

            assertThat(response.answer()).isEqualTo("Kullanıcı'nın önerisi");
        }
    }

    @Nested
    @DisplayName("N8n event publishing")
    class N8nEventTests {

        @Test
        @DisplayName("Should publish N8n event on chat")
        void chat_ShouldPublishN8nEvent() {
            mockChatClientChain("{\"answer\": \"Önerim\", \"recommendedProducts\": []}");

            assistantService.chat("laptop öner", 1L, "test@test.com", "web");

            verify(n8nEventPublisher).publishConversationCompletedEvent(
                    eq(1L), eq("test@test.com"), eq("laptop öner"),
                    eq("Önerim"), anyList(), eq("web")
            );
        }

        @Test
        @DisplayName("Should still return response when N8n publisher fails")
        void chat_WhenN8nPublisherFails_ShouldStillReturnResponse() {
            mockChatClientChain("{\"answer\": \"Önerim\", \"recommendedProducts\": []}");
            doThrow(new RuntimeException("n8n down")).when(n8nEventPublisher)
                    .publishConversationCompletedEvent(any(), any(), any(), any(), any(), any());

            AssistantResponse response = assistantService.chat("laptop öner", 1L, "test@test.com", "web");

            assertThat(response.answer()).isEqualTo("Önerim");
        }
    }

    @Nested
    @DisplayName("Source parameter handling")
    class SourceParameterTests {

        @Test
        @DisplayName("Should default source to ecommerce_chat when null")
        void chat_WhenSourceIsNull_ShouldDefaultToEcommerceChat() {
            mockChatClientChain("{\"answer\": \"Önerim\", \"recommendedProducts\": []}");

            assistantService.chat("laptop öner", 1L, "test@test.com", null);

            verify(n8nEventPublisher).publishConversationCompletedEvent(
                    eq(1L), eq("test@test.com"), eq("laptop öner"),
                    eq("Önerim"), anyList(), eq("ecommerce_chat")
            );
        }

        @Test
        @DisplayName("Should default source to ecommerce_chat when blank")
        void chat_WhenSourceIsBlank_ShouldDefaultToEcommerceChat() {
            mockChatClientChain("{\"answer\": \"Önerim\", \"recommendedProducts\": []}");

            assistantService.chat("laptop öner", 1L, "test@test.com", "   ");

            verify(n8nEventPublisher).publishConversationCompletedEvent(
                    eq(1L), eq("test@test.com"), eq("laptop öner"),
                    eq("Önerim"), anyList(), eq("ecommerce_chat")
            );
        }

        @Test
        @DisplayName("Should delegate single param overload with defaults")
        void chat_SingleParamOverload_ShouldDelegateWithDefaults() {
            mockChatClientChain("{\"answer\": \"Önerim\", \"recommendedProducts\": []}");

            assistantService.chat("laptop öner");

            verify(n8nEventPublisher).publishConversationCompletedEvent(
                    isNull(), isNull(), eq("laptop öner"),
                    eq("Önerim"), anyList(), eq("ecommerce_chat")
            );
        }
    }

    @Nested
    @DisplayName("sanitizeJsonLikeString helper")
    class SanitizeHelperTests {

        @Test
        @DisplayName("Should return empty JSON for null input")
        void sanitizeJsonLikeString_WhenNull_ShouldReturnEmptyJson() throws Exception {
            assertThat(invokeSanitize(null)).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should wrap as JSON answer when no braces found")
        void sanitizeJsonLikeString_WhenNoBraces_ShouldWrapAsJsonAnswer() throws Exception {
            assertThat(invokeSanitize("no json here"))
                    .isEqualTo("{\"answer\":\"no json here\",\"recommendedProducts\":[]}");
        }

        @Test
        @DisplayName("Should handle empty string input")
        void sanitizeJsonLikeString_WhenEmpty_ShouldWrapAsJsonAnswer() throws Exception {
            String result = invokeSanitize("");
            assertThat(result).contains("answer");
        }

        @Test
        @DisplayName("Should escape quotes in non-JSON response")
        void sanitizeJsonLikeString_WhenContainsQuotes_ShouldEscapeQuotes() throws Exception {
            String result = invokeSanitize("this has \"quotes\" in it");
            assertThat(result).contains("\\\"quotes\\\"");
        }

        @Test
        @DisplayName("Should handle backslash in non-JSON response")
        void sanitizeJsonLikeString_WhenContainsBackslash_ShouldEscape() throws Exception {
            String result = invokeSanitize("path\\to\\file");
            assertThat(result).contains("answer");
        }

        @Test
        @DisplayName("Should remove invalid escape sequences from JSON")
        void sanitizeJsonLikeString_WhenInvalidEscapeInJson_ShouldClean() throws Exception {
            String result = invokeSanitize("{\"answer\": \"test\\x value\"}");
            assertThat(result).contains("testx value");
        }

        @Test
        @DisplayName("Should remove TL from price")
        void sanitizeJsonLikeString_WhenPriceWithTL_ShouldRemoveTL() throws Exception {
            assertThat(invokeSanitize("{\"price\": \"500 TL\"}")).isEqualTo("{\"price\": \"500\"}");
        }

        @Test
        @DisplayName("Should remove TL from decimal price")
        void sanitizeJsonLikeString_WhenPriceWithDecimalTL_ShouldRemoveTL() throws Exception {
            assertThat(invokeSanitize("{\"price\": \"199.99 TL\"}")).isEqualTo("{\"price\": \"199.99\"}");
        }
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
