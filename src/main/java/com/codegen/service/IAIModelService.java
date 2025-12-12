package com.codegen.service;

/**
 * Интерфейс для работы с AI моделью
 */
public interface IAIModelService {
    String analyzeRequirements(String description, String language, String framework);
    String generateCode(String structuredModel, String language, String framework, String templateContent);
    String validateAndOptimize(String sourceCode, String language);
    String clarifyRequirements(String originalRequirement, String context);
}