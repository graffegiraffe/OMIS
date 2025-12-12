package com.codegen.model;

import lombok.Data;

@Data
public class OptimizationSuggestion {
    private String type;
    private String description;
    private String currentCode;
    private String suggestedCode;
    private Integer priority;
}