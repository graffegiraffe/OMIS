package com.codegen.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_reports")
@Data
public class ValidationReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_code_id")
    private GeneratedCode generatedCode;

    @Column(nullable = false)
    private Boolean hasErrors;

    @Column(nullable = false)
    private Boolean hasSyntaxErrors;

    @Column(columnDefinition = "TEXT")
    private String errorDetails; // JSON ошибок

    @Column(columnDefinition = "TEXT")
    private String optimizationSuggestions; // JSON предложений

    @Column(nullable = false)
    private Integer qualityScore;

    @Column(nullable = false)
    private LocalDateTime validatedAt;

    @PrePersist
    protected void onCreate() {
        validatedAt = LocalDateTime.now();
        if (hasErrors == null) {
            hasErrors = false;
        }
        if (hasSyntaxErrors == null) {
            hasSyntaxErrors = false;
        }
        if (qualityScore == null) {
            qualityScore = 0;
        }
    }
}