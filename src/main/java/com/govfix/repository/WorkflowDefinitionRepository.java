package com.govfix.repository;

import com.govfix.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    Optional<WorkflowDefinition> findByWorkflowKey(String workflowKey);
    Optional<WorkflowDefinition> findFirstByCategoryPatternContainingIgnoreCase(String category);
}

