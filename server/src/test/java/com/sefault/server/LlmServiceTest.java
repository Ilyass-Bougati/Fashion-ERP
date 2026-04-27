package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.sefault.server.ai.service.LlmService;
import com.sefault.server.ai.service.impl.LlmServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private LlmService llmService;

    @BeforeEach
    void setUp() {
        llmService = new LlmServiceImpl(chatClient);
    }

    // Helper to stub the full happy-path chain
    private void stubFullChain() {
        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
    }

    @Test
    @DisplayName("prompt() returns the content from the LLM response")
    void prompt_returnsLlmContent() {
        stubFullChain();
        when(callResponseSpec.content()).thenReturn("Hello from LLM!");

        String result = llmService.prompt("Say hello");

        assertThat(result).isEqualTo("Hello from LLM!");
    }

    @Test
    @DisplayName("prompt() passes the exact prompt string to the ChatClient")
    void prompt_passesPromptToChatClient() {
        stubFullChain();
        when(callResponseSpec.content()).thenReturn("some response");
        String userPrompt = "What is the capital of France?";

        llmService.prompt(userPrompt);

        verify(chatClient).prompt(userPrompt);
    }

    @Test
    @DisplayName("prompt() returns null when LLM responds with no content")
    void prompt_returnsNullWhenContentIsNull() {
        stubFullChain();
        when(callResponseSpec.content()).thenReturn(null);

        String result = llmService.prompt("Empty response test");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("prompt() returns an empty string when LLM responds with empty content")
    void prompt_returnsEmptyStringWhenContentIsEmpty() {
        stubFullChain();
        when(callResponseSpec.content()).thenReturn("");

        String result = llmService.prompt("Empty string test");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("prompt() propagates exceptions thrown by the ChatClient")
    void prompt_propagatesExceptionFromChatClient() {
        // Chain breaks at the first call — requestSpec and callResponseSpec stubs are not needed
        when(chatClient.prompt(anyString())).thenThrow(new RuntimeException("LLM unavailable"));

        assertThatThrownBy(() -> llmService.prompt("Failing prompt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("LLM unavailable");
    }

    @Test
    @DisplayName("prompt() propagates exceptions thrown during .call()")
    void prompt_propagatesExceptionFromCall() {
        // Chain breaks at .call() — callResponseSpec stub is not needed
        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("Call failed"));

        assertThatThrownBy(() -> llmService.prompt("Failing call"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Call failed");
    }

    @Test
    @DisplayName("prompt() handles a multi-line prompt without altering it")
    void prompt_handlesMultiLinePrompt() {
        stubFullChain();
        String multiLine = "Line one\nLine two\nLine three";
        when(callResponseSpec.content()).thenReturn("Multi-line response");

        String result = llmService.prompt(multiLine);

        verify(chatClient).prompt(multiLine);
        assertThat(result).isEqualTo("Multi-line response");
    }
}