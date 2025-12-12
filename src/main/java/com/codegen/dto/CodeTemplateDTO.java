package com.codegen.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CodeTemplateDTO {
    private Long id;
    private String name;
    private String description;
    private String language;
    private String framework;
    private String type;
    private Integer usageCount;
    private LocalDateTime updatedAt;
}
