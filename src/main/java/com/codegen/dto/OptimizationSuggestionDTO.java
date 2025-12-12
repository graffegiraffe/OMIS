package com.codegen.dto;

import lombok.Data;

@Data
public class OptimizationSuggestionDTO {
    private String type;
    private String description;
    private String currentCode;
    private String suggestedCode;
    private Integer priority;
}