package com.codegen.service;

import com.codegen.model.CodeTemplate;
import com.codegen.model.ValidationReport;
import com.codegen.repository.CodeTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления шаблонами
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateManagementService {

    private final CodeTemplateRepository templateRepository;

    /**
     * Добавление нового шаблона в базу
     */
    public CodeTemplate addTemplate(CodeTemplate template) {
        log.info("Adding new template: {} for language: {}",
                template.getName(), template.getLanguage());
        return templateRepository.save(template);
    }

    /**
     * Обновление существующего шаблона
     */
    public CodeTemplate updateTemplate(Long id, CodeTemplate updatedTemplate) {
        CodeTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));

        existing.setName(updatedTemplate.getName());
        existing.setDescription(updatedTemplate.getDescription());
        existing.setTemplateContent(updatedTemplate.getTemplateContent());
        existing.setVariables(updatedTemplate.getVariables());

        log.info("Updated template: {}", id);
        return templateRepository.save(existing);
    }

    /**
     * Автоматическое обновление базы
     */
    public void autoUpdateTemplates(ValidationReport report) {
        if (report.getHasErrors() && report.getGeneratedCode() != null) {
            CodeTemplate template = report.getGeneratedCode().getTemplate();

            if (template != null) {
                log.info("Analyzing errors for template: {}", template.getId());
            }
        }
    }

    /**
     * Получение наиболее используемых шаблонов
     */
    public List<CodeTemplate> getMostUsedTemplates(String language, int limit) {
        return templateRepository.findByLanguage(language)
                .stream()
                .sorted((t1, t2) -> t2.getUsageCount().compareTo(t1.getUsageCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}