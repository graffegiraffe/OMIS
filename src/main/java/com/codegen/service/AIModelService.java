package com.codegen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIModelService implements IAIModelService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = "You are a code generation AI. Respond only with the requested output.";

    @Retry(name = "groqApi")  //3 раза повтор
    public String analyzeRequirements(String description, String language, String framework) {
        String prompt = String.format(
                "Analyze requirement: '%s'. Language: %s, Framework: %s. Output structured JSON model with entities, actions, parameters.",
                description, language, framework
        );

        return callGroqApi(prompt);
    }

    @Retry(name = "groqApi")
    public String generateCode(String structuredModel, String language, String framework, String templateContent) {
        String prompt = String.format(
                "Generate code based on model: %s. Language: %s, Framework: %s. Use template: %s. Output only code.",
                structuredModel, language, framework, templateContent
        );

        return callGroqApi(prompt);
    }

    @Retry(name = "groqApi")
    public String validateAndOptimize(String sourceCode, String language) {
        String prompt = String.format(
                "Validate and optimize code: %s. Language: %s. Output JSON with hasErrors, errors[], suggestions[], qualityScore.",
                sourceCode, language
        );

        return callGroqApi(prompt);
    }

    @Retry(name = "groqApi")
    public String clarifyRequirements(String originalRequirement, String context) {
        String prompt = String.format(
                "Clarify requirement: %s. Context: %s. Suggest questions for clarification.",
                originalRequirement, context
        );

        return callGroqApi(prompt);
    }

    private String callGroqApi(String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectNode requestJson = objectMapper.createObjectNode();
            requestJson.put("model", model);
            ArrayNode messages = requestJson.putArray("messages");

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            messages.add(systemMessage);

            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);

            HttpEntity<String> request = new HttpEntity<>(requestJson.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String content = jsonResponse.path("choices").get(0).path("message").path("content").asText();
                return cleanMarkdown(content);
            } else {
                throw new RuntimeException("Groq API error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error calling Groq API: ", e);
            throw new RuntimeException("Failed to call Groq API: " + e.getMessage());
        }
    }

    private String cleanMarkdown(String text) {
        if (text == null) return "";
        return text.replaceAll("```[a-z]*", "").replace("```", "").trim();
    }
}