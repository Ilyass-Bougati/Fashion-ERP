package com.sefault.server.ai.controller;

import com.sefault.server.ai.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/dev/llm")
@RequiredArgsConstructor
public class LlmTestController {
    private final LlmService llmService;

    @PostMapping
    public String prompt(@RequestBody String prompt) {
        return llmService.prompt(prompt);
    }
}
