package com.codegen.mapper;

import com.codegen.dto.ErrorDetailDTO;
import com.codegen.dto.OptimizationSuggestionDTO;
import com.codegen.dto.ValidationReportDTO;
import com.codegen.model.ErrorDetail;
import com.codegen.model.OptimizationSuggestion;
import com.codegen.model.ValidationReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationReportMapper {

    private final ObjectMapper objectMapper;

    public ValidationReportDTO toDTO(ValidationReport report) {
        if (report == null) {
            return null;
        }

        ValidationReportDTO dto = new ValidationReportDTO();
        dto.setHasErrors(report.getHasErrors());
        dto.setHasSyntaxErrors(report.getHasSyntaxErrors());
        dto.setQualityScore(report.getQualityScore());

        try {
            //парсинг ошибок из JSON
            if (report.getErrorDetails() != null) {
                List<ErrorDetail> errors = objectMapper.readValue(
                        report.getErrorDetails(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ErrorDetail.class)
                );
                dto.setErrors(mapErrorDetailsToDTO(errors));
            }

            // парсинг предложений из JSON
            if (report.getOptimizationSuggestions() != null) {
                List<OptimizationSuggestion> suggestions = objectMapper.readValue(
                        report.getOptimizationSuggestions(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, OptimizationSuggestion.class)
                );
                dto.setSuggestions(mapSuggestionsToDTO(suggestions));
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing validation report JSON: {}", e.getMessage());
        }

        return dto;
    }

    private List<ErrorDetailDTO> mapErrorDetailsToDTO(List<ErrorDetail> errors) {
        if (errors == null) {
            return new ArrayList<>();
        }

        return errors.stream()
                .map(this::mapErrorDetailToDTO)
                .toList();
    }

    private ErrorDetailDTO mapErrorDetailToDTO(ErrorDetail error) {
        ErrorDetailDTO dto = new ErrorDetailDTO();
        dto.setType(error.getType());
        dto.setMessage(error.getMessage());
        dto.setLine(error.getLine());
        dto.setSeverity(error.getSeverity());
        return dto;
    }

    private List<OptimizationSuggestionDTO> mapSuggestionsToDTO(List<OptimizationSuggestion> suggestions) {
        if (suggestions == null) {
            return new ArrayList<>();
        }

        return suggestions.stream()
                .map(this::mapSuggestionToDTO)
                .toList();
    }

    private OptimizationSuggestionDTO mapSuggestionToDTO(OptimizationSuggestion suggestion) {
        OptimizationSuggestionDTO dto = new OptimizationSuggestionDTO();
        dto.setType(suggestion.getType());
        dto.setDescription(suggestion.getDescription());
        dto.setCurrentCode(suggestion.getCurrentCode());
        dto.setSuggestedCode(suggestion.getSuggestedCode());
        dto.setPriority(suggestion.getPriority());
        return dto;
    }
}