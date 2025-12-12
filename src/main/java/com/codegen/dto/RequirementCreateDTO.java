package com.codegen.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class RequirementCreateDTO {
    @NotBlank(message = "Описание требований обязательно")
    @Size(min = 10, max = 2000, message = "Описание должно быть от 10 до 2000 символов")
    private String description;

    @NotBlank(message = "Язык программирования обязателен")
    private String language; // java, python, javascript

    private String framework; // spring, django, react, vue

    private Long projectId;
}