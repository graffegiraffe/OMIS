package com.codegen.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RequirementResponseDTO {
    private Long id;
    private String description;
    private String language;
    private String framework;
    private String status;
    private String structuredModel;
    private LocalDateTime createdAt;
    private List<GeneratedCodeDTO> generatedCodes;
}