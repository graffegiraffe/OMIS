package com.codegen.model;

import lombok.Data;

@Data
public class ErrorDetail {
    private String type;
    private String message;
    private Integer line;
    private String severity;
}