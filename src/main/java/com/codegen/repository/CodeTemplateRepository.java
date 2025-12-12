package com.codegen.repository;

import com.codegen.model.CodeTemplate;
import com.codegen.model.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeTemplateRepository extends JpaRepository<CodeTemplate, Long> {
    List<CodeTemplate> findByLanguageAndFramework(String language, String framework);
    List<CodeTemplate> findByLanguage(String language);
    List<CodeTemplate> findByType(TemplateType type);

    @Query("SELECT t FROM CodeTemplate t WHERE t.language = :language AND t.type = :type ORDER BY t.usageCount DESC")
    List<CodeTemplate> findMostUsedTemplates(String language, TemplateType type);
}