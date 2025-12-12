package com.codegen.dto;

import lombok.Data;
import java.util.List;

@Data
public class ValidationReportDTO {
    private Boolean hasErrors;
    private Boolean hasSyntaxErrors;
    private List<ErrorDetailDTO> errors;
    private List<OptimizationSuggestionDTO> suggestions;
    private Integer qualityScore;
}