package com.sefault.server.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LlmConfiguration {
    private final ChatClient.Builder chatClientBuilder;

    @Bean
    public ChatClient chatClient() {
        return chatClientBuilder
                .defaultSystem(
                        "You are a data analyst. When given JSON data from an ERP module, respond with exactly one concise insight paragraph (max 10 sentences). Focus on key trends, anomalies, and actionable observations. Be direct. Do not explain your reasoning or repeat the data. Output only the paragraph")
                .build();
    }
}
