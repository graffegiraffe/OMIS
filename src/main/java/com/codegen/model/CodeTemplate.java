package com.codegen.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_templates")
@Data
public class CodeTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; //название шаблона

    @Column(length = 1000)
    private String description; //описание

    @Column(nullable = false)
    private String language; //язык программирования

    private String framework; //фреймворк

    @Enumerated(EnumType.STRING)
    private TemplateType type; //тип

    @Column(nullable = false, columnDefinition = "TEXT")
    private String templateContent; //содержимое

    @Column(columnDefinition = "TEXT")
    private String variables; // JSON переменных для подстановки

    @Column(nullable = false)
    private Integer usageCount = 0; //счетчик использований

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}