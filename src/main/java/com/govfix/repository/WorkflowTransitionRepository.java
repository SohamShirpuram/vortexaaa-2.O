package com.govfix.repository;

import com.govfix.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
    List<WorkflowTransition> findByWorkflowId(Long workflowId);
    List<WorkflowTransition> findByWorkflowIdAndFromStateKey(Long workflowId, String fromStateKey);
    Optional<WorkflowTransition> findByWorkflowIdAndFromStateKeyAndToStateKey(Long workflowId, String fromStateKey, String toStateKey);
}

