package com.codegen.repository;

import com.codegen.model.Requirement;
import com.codegen.model.RequirementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long> {
    List<Requirement> findByUserId(Long userId);
    List<Requirement> findByProjectId(Long projectId);
    List<Requirement> findByStatus(RequirementStatus status);

    @Query("SELECT r FROM Requirement r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Requirement> findRecentByUserId(Long userId);
}
