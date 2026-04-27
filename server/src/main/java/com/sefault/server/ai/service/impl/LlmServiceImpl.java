package com.sefault.server.ai.service.impl;

import com.sefault.server.ai.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LlmServiceImpl implements LlmService {
    private final ChatClient chatClient;

    @Override
    public String prompt(String prompt) {
        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
