package com.codegen.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GeneratedCodeDTO {
    private Long id;
    private String sourceCode;
    private String fileName;
    private String packagePath;
    private LocalDateTime generatedAt;
    private ValidationReportDTO validationReport;
}