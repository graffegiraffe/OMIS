package com.codegen.dto;

import lombok.Data;

@Data
public class ErrorDetailDTO {
    private String type;
    private String message;
    private Integer line;
    private String severity;
}