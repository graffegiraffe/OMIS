package com.codegen.controller;

import com.codegen.dto.GeneratedCodeDTO;
import com.codegen.dto.RequirementCreateDTO;
import com.codegen.dto.RequirementResponseDTO;
import com.codegen.mapper.GeneratedCodeMapper;
import com.codegen.model.GeneratedCode;
import com.codegen.model.Requirement;
import com.codegen.model.RequirementStatus;
import com.codegen.model.User;
import com.codegen.repository.ProjectRepository;
import com.codegen.repository.RequirementRepository;
import com.codegen.repository.UserRepository;
import com.codegen.service.CodeGenerationService;
import com.codegen.service.CodeValidationService;
import com.codegen.service.RequirementAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/code-generation")
@RequiredArgsConstructor
@Slf4j
public class CodeGenerationController {

    private final RequirementAnalysisService analysisService;
    private final CodeGenerationService generationService;
    private final CodeValidationService validationService;
    private final RequirementRepository requirementRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final GeneratedCodeMapper generatedCodeMapper;

    @PostMapping("/generate")
    public ResponseEntity<?> generateCode(
            @Valid @RequestBody RequirementCreateDTO dto,
            @RequestHeader("User-Id") Long userId) {

        try {
            log.info("Received code generation request from user: {}", userId);

            //валидация пользователя
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            //создание требования
            Requirement requirement = createRequirement(dto, user);

            //асинхронная обработка
            processRequirementAsync(requirement);

            return ResponseEntity.ok(mapToDTO(requirement));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error processing code generation request: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/status/{requirementId}")
    public ResponseEntity<?> getStatus(@PathVariable Long requirementId) {
        try {
            Requirement requirement = requirementRepository.findById(requirementId)
                    .orElseThrow(() -> new IllegalArgumentException("Requirement not found"));

            return ResponseEntity.ok(mapToDTO(requirement));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/code/{requirementId}")
    public ResponseEntity<?> getGeneratedCode(@PathVariable Long requirementId) {
        try {
            Requirement requirement = requirementRepository.findById(requirementId)
                    .orElseThrow(() -> new IllegalArgumentException("Requirement not found"));

            List<GeneratedCodeDTO> codes = requirement.getGeneratedCodes().stream()
                    .map(generatedCodeMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(codes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Code Generation API is running");
    }

    private Requirement createRequirement(RequirementCreateDTO dto, User user) {
        Requirement requirement = new Requirement();
        requirement.setDescription(dto.getDescription());
        requirement.setLanguage(dto.getLanguage());
        requirement.setFramework(dto.getFramework());
        requirement.setUser(user);
        requirement.setStatus(RequirementStatus.PENDING);

        if (dto.getProjectId() != null) {
            projectRepository.findById(dto.getProjectId())
                    .ifPresent(requirement::setProject);
        }

        return requirementRepository.save(requirement);
    }

    private void processRequirementAsync(Requirement requirement) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async processing for requirement: {}", requirement.getId());

                //анализ требований
                log.info("Step 1: Analyzing requirement {}", requirement.getId());
                Requirement analyzed = analysisService.analyzeRequirement(requirement);
                log.info("Analysis completed for requirement {}", requirement.getId());

                //генерация кода
                log.info("Step 2: Generating code for requirement {}", requirement.getId());
                List<GeneratedCode> codes = generationService.generateCode(analyzed);
                log.info("Generated {} code files for requirement {}",
                        codes.size(), requirement.getId());

                //валидация
                log.info("Step 3: Validating code for requirement {}", requirement.getId());
                validationService.validateRequirementCodes(analyzed);
                log.info("Validation completed for requirement {}", requirement.getId());

                log.info("Successfully processed requirement: {}", requirement.getId());

            } catch (Exception e) {
                log.error("Error in code generation pipeline for requirement {}: ",
                        requirement.getId(), e);

                requirement.setStatus(RequirementStatus.FAILED);
                requirementRepository.save(requirement);
            }
        });
    }

    private RequirementResponseDTO mapToDTO(Requirement req) {
        RequirementResponseDTO dto = new RequirementResponseDTO();
        dto.setId(req.getId());
        dto.setDescription(req.getDescription());
        dto.setLanguage(req.getLanguage());
        dto.setFramework(req.getFramework());
        dto.setStatus(req.getStatus().name());
        dto.setStructuredModel(req.getStructuredModel());
        dto.setCreatedAt(req.getCreatedAt());

        if (req.getGeneratedCodes() != null && !req.getGeneratedCodes().isEmpty()) {
            dto.setGeneratedCodes(
                    req.getGeneratedCodes().stream()
                            .map(generatedCodeMapper::toDTO)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }
}