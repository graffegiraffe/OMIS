package com.codegen.mapper;

import com.codegen.dto.GeneratedCodeDTO;
import com.codegen.model.GeneratedCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeneratedCodeMapper {

    private final ValidationReportMapper validationReportMapper;

    public GeneratedCodeDTO toDTO(GeneratedCode code) {
        if (code == null) {
            return null;
        }

        GeneratedCodeDTO dto = new GeneratedCodeDTO();
        dto.setId(code.getId());
        dto.setSourceCode(code.getSourceCode());
        dto.setFileName(code.getFileName());
        dto.setPackagePath(code.getPackagePath());
        dto.setGeneratedAt(code.getGeneratedAt());

        //маппинг валидационного отчета
        if (code.getValidationReport() != null) {
            dto.setValidationReport(
                    validationReportMapper.toDTO(code.getValidationReport())
            );
        }

        return dto;
    }
}