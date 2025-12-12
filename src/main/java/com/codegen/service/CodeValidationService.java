package com.codegen.service;

import com.codegen.model.GeneratedCode;
import com.codegen.model.Requirement;
import com.codegen.model.RequirementStatus;
import com.codegen.model.ValidationReport;
import com.codegen.repository.GeneratedCodeRepository;
import com.codegen.repository.RequirementRepository;
import com.codegen.repository.ValidationReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис валидации и оптимизации кода
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CodeValidationService {

    private final AIModelService aiModelService;
    private final ValidationReportRepository reportRepository;
    private final GeneratedCodeRepository codeRepository;
    private final RequirementRepository requirementRepository;
    private final ObjectMapper objectMapper;

    /**
     * Валидация сгенерированного кода
     */
    @Transactional
    public ValidationReport validateCode(GeneratedCode generatedCode) {
        try {
            String language = generatedCode.getRequirement().getLanguage();

            String validationResult = aiModelService.validateAndOptimize(
                    generatedCode.getSourceCode(),
                    language
            );

            //парсинг JSON ответа
            JsonNode resultNode = objectMapper.readTree(validationResult);

            ValidationReport report = new ValidationReport();
            report.setGeneratedCode(generatedCode);
            report.setHasSyntaxErrors(resultNode.has("hasSyntaxErrors") && resultNode.get("hasSyntaxErrors").asBoolean());
            report.setHasErrors(resultNode.has("errors") && resultNode.get("errors").size() > 0);
            report.setErrorDetails(resultNode.has("errors") ? resultNode.get("errors").toString() : "[]");
            report.setOptimizationSuggestions(resultNode.has("suggestions") ? resultNode.get("suggestions").toString() : "[]");

            int score = 0;
            if (resultNode.has("qualityScore")) {
                score = resultNode.get("qualityScore").asInt();
            }
            report.setQualityScore(score);

            report = reportRepository.save(report);

            log.info("Validation completed for code {}, quality score: {}",
                    generatedCode.getId(), report.getQualityScore());

            return report;

        } catch (Exception e) {
            log.error("Error validating code {}: ", generatedCode.getId(), e);
            throw new RuntimeException("Failed to validate code: " + e.getMessage());
        }
    }

    /**
     * Валидация всех кодов для требования
     */
    @Transactional
    public void validateRequirementCodes(Requirement requirement) {
        try {
            List<GeneratedCode> codes = codeRepository.findByRequirementId(requirement.getId());

            for (GeneratedCode code : codes) {
                validateCode(code);
            }

            //обновляем статус
            Requirement currentReq = requirementRepository.findById(requirement.getId())
                    .orElseThrow(() -> new RuntimeException("Requirement not found"));

            currentReq.setStatus(RequirementStatus.COMPLETED);
            requirementRepository.save(currentReq);

            log.info("All codes validated for requirement {}", requirement.getId());

        } catch (Exception e) {
            log.error("Error during validation: ", e);
            updateStatusToFailed(requirement.getId());
            throw new RuntimeException("Validation failed: " + e.getMessage());
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void updateStatusToFailed(Long requirementId) {
        requirementRepository.findById(requirementId).ifPresent(req -> {
            req.setStatus(RequirementStatus.FAILED);
            requirementRepository.save(req);
        });
    }

    /**
     * Проверка соответствия стандартам кодирования
     */
    @Transactional(readOnly = true)
    public boolean checkCodingStandards(GeneratedCode code) {
        ValidationReport report = reportRepository.findByGeneratedCodeId(code.getId())
                .orElse(null);

        return report != null &&
                !report.getHasSyntaxErrors() &&
                report.getQualityScore() >= 70;
    }
}