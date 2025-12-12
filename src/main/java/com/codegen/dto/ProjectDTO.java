package com.codegen.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String status;
    private String language;
    private String framework;
    private LocalDateTime updatedAt;
    private int filesCount;
    private int linesOfCode;
}