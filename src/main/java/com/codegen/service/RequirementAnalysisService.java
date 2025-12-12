package com.codegen.service;

import com.codegen.model.Requirement;
import com.codegen.model.RequirementStatus;
import com.codegen.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Сервис подсистемы анализа требований
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RequirementAnalysisService {

    private final AIModelService aiModelService;
    private final RequirementRepository requirementRepository;
    private final ObjectMapper objectMapper;

    /**
     * Анализ и структурирование требований
     */
    public Requirement analyzeRequirement(Requirement requirement) {
        try {
            requirement.setStatus(RequirementStatus.ANALYZING);
            requirement = requirementRepository.save(requirement);

            //AI для анализа
            String structuredModel = aiModelService.analyzeRequirements(
                    requirement.getDescription(),
                    requirement.getLanguage(),
                    requirement.getFramework()
            );

            requirement.setStructuredModel(structuredModel);
            requirement.setStatus(RequirementStatus.COMPLETED);

            log.info("Requirement {} analyzed successfully", requirement.getId());
            return requirementRepository.save(requirement);

        } catch (Exception e) {
            log.error("Error analyzing requirement {}: ", requirement.getId(), e);
            requirement.setStatus(RequirementStatus.FAILED);
            requirementRepository.save(requirement);
            throw new RuntimeException("Failed to analyze requirement: " + e.getMessage());
        }
    }

    /**
     * Проверка полноты требований
     */
    public boolean isRequirementComplete(String description) {
        //проверка на полноту описания
        return description != null &&
                description.length() >= 20 &&
                (description.contains("создать") ||
                        description.contains("сделать") ||
                        description.contains("разработать") ||
                        description.contains("реализовать"));
    }

    /**
     * Запрос уточнений у пользователя
     */
    public String requestClarification(Requirement requirement) {
        return aiModelService.clarifyRequirements(
                requirement.getDescription(),
                "Language: " + requirement.getLanguage() +
                        ", Framework: " + requirement.getFramework()
        );
    }
}