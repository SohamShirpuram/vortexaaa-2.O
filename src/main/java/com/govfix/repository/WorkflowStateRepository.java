package com.govfix.repository;

import com.govfix.entity.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, Long> {
    List<WorkflowState> findByWorkflowIdOrderByStageOrderAsc(Long workflowId);
    Optional<WorkflowState> findByWorkflowIdAndStateKey(Long workflowId, String stateKey);
}
