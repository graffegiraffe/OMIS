package com.codegen.controller;

import com.codegen.dto.CodeTemplateDTO;
import com.codegen.model.CodeTemplate;
import com.codegen.repository.CodeTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final CodeTemplateRepository templateRepository;

    //получить все шаблоны
    @GetMapping
    public ResponseEntity<List<CodeTemplateDTO>> getAllTemplates() {
        List<CodeTemplateDTO> dtos = templateRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //скачать шаблон как файл
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable Long id) {
        CodeTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        //увеличиваем счетчик
        template.setUsageCount(template.getUsageCount() + 1);
        templateRepository.save(template);

        //создаем файл
        byte[] content = template.getTemplateContent().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(content);

        //определяем расширение
        String extension = getExtension(template.getLanguage());
        String filename = template.getName().replaceAll("\\s+", "_") + extension;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(content.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private CodeTemplateDTO mapToDTO(CodeTemplate template) {
        CodeTemplateDTO dto = new CodeTemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setLanguage(template.getLanguage());
        dto.setFramework(template.getFramework());
        dto.setType(template.getType() != null ? template.getType().name() : "OTHER");
        dto.setUsageCount(template.getUsageCount());
        dto.setUpdatedAt(template.getUpdatedAt());
        return dto;
    }

    private String getExtension(String language) {
        if (language == null) return ".txt";
        return switch (language.toLowerCase()) {
            case "java" -> ".java";
            case "python" -> ".py";
            case "javascript" -> ".js";
            case "typescript" -> ".ts";
            case "html" -> ".html";
            case "css" -> ".css";
            case "php" -> ".php";
            default -> ".txt";
        };
    }
}
