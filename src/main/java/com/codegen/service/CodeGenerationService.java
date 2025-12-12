package com.codegen.service;

import com.codegen.model.CodeTemplate;
import com.codegen.model.GeneratedCode;
import com.codegen.model.Requirement;
import com.codegen.model.RequirementStatus;
import com.codegen.repository.CodeTemplateRepository;
import com.codegen.repository.GeneratedCodeRepository;
import com.codegen.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис генерации кода
 * Реализует подсистему генерации кода
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CodeGenerationService {

    private final AIModelService aiModelService;
    private final CodeTemplateRepository templateRepository;
    private final GeneratedCodeRepository generatedCodeRepository;
    private final RequirementRepository requirementRepository;

    /**
     * Генерация кода для требования
     */
    public List<GeneratedCode> generateCode(Requirement requirement) {
        try {
            requirement.setStatus(RequirementStatus.GENERATING);
            requirementRepository.save(requirement);

            //получение шаблонов из базы знаний
            List<CodeTemplate> templates = findSuitableTemplates(
                    requirement.getLanguage(),
                    requirement.getFramework()
            );

            List<GeneratedCode> generatedCodes = new ArrayList<>();

            //генерация кода
            for (CodeTemplate template : templates) {
                String sourceCode = aiModelService.generateCode(
                        requirement.getStructuredModel(),
                        requirement.getLanguage(),
                        requirement.getFramework(),
                        template.getTemplateContent()
                );

                GeneratedCode code = new GeneratedCode();
                code.setSourceCode(sourceCode);
                code.setFileName(generateFileName(template, requirement));
                code.setPackagePath(generatePackagePath(requirement));
                code.setRequirement(requirement);
                code.setTemplate(template);

                generatedCodes.add(generatedCodeRepository.save(code));

                //увеличение счетчика
                template.setUsageCount(template.getUsageCount() + 1);
                templateRepository.save(template);
            }

            requirement.setStatus(RequirementStatus.VALIDATING);
            requirementRepository.save(requirement);

            log.info("Generated {} code files for requirement {}",
                    generatedCodes.size(), requirement.getId());

            return generatedCodes;

        } catch (Exception e) {
            log.error("Error generating code for requirement {}: ", requirement.getId(), e);
            requirement.setStatus(RequirementStatus.FAILED);
            requirementRepository.save(requirement);
            throw new RuntimeException("Failed to generate code: " + e.getMessage());
        }
    }

    /**
     * Поиск подходящих шаблонов из базы знаний
     */
    private List<CodeTemplate> findSuitableTemplates(String language, String framework) {
        List<CodeTemplate> templates;

        if (framework != null && !framework.isEmpty()) {
            templates = templateRepository.findByLanguageAndFramework(language, framework);
        } else {
            templates = templateRepository.findByLanguage(language);
        }

        //если нет, возвращаем базовые шаблоны
        if (templates.isEmpty()) {
            log.warn("No templates found for language: {}, framework: {}", language, framework);
            templates = getDefaultTemplates(language);
        }

        return templates;
    }

    private String generateFileName(CodeTemplate template, Requirement requirement) {
        String baseName = template.getType().name().toLowerCase();
        String extension = getFileExtension(requirement.getLanguage());
        return baseName + "_generated." + extension;
    }

    private String generatePackagePath(Requirement requirement) {
        return "com.generated." + requirement.getLanguage().toLowerCase();
    }

    private String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> "java";
            case "python" -> "py";
            case "javascript", "typescript" -> "js";
            case "kotlin" -> "kt";
            default -> "txt";
        };
    }
    private List<CodeTemplate> getDefaultTemplates(String language) {
        List<CodeTemplate> templates = new ArrayList<>();
        if ("typescript".equalsIgnoreCase(language)) {
            CodeTemplate defaultReact = new CodeTemplate();
            defaultReact.setName("Default React");
            defaultReact.setTemplateContent("import React from 'react';\nconst Component = () => <div>Generated</div>;");
            templates.add(defaultReact);
        }
        return templates;
    }
}