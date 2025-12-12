package com.codegen.repository;

import com.codegen.model.ValidationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValidationReportRepository extends JpaRepository<ValidationReport, Long> {
    Optional<ValidationReport> findByGeneratedCodeId(Long generatedCodeId);
}
