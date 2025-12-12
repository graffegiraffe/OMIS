package com.codegen.repository;

import com.codegen.model.GeneratedCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedCodeRepository extends JpaRepository<GeneratedCode, Long> {
    List<GeneratedCode> findByRequirementId(Long requirementId);
}
