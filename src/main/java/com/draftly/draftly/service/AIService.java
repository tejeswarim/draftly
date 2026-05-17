package com.draftly.draftly.service;

import com.draftly.draftly.config.OpenAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OpenAIConfig openAIConfig;

    public String generateReply(String emailBody) {

        String url = "https://openrouter.ai/api/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(openAIConfig.getApiKey());

        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "openai/gpt-3.5-turbo",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You are an email assistant."
                        ),
                        Map.of(
                                "role", "user",
                                "content", "Generate professional reply for this email:\n" + emailBody
                        )
                )
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) response.getBody().get("choices");

        Map<String, Object> choice = choices.get(0);

        Map<String, String> message =
                (Map<String, String>) choice.get("message");

        return message.get("content");
    }
}