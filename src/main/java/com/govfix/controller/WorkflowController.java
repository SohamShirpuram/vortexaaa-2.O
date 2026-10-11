package com.govfix.controller;

import com.govfix.dto.ApiResponse;
import com.govfix.dto.WorkflowConfigRequest;
import com.govfix.entity.WorkflowDefinition;
import com.govfix.entity.WorkflowState;
import com.govfix.entity.WorkflowTransition;
import com.govfix.repository.WorkflowDefinitionRepository;
import com.govfix.repository.WorkflowStateRepository;
import com.govfix.repository.WorkflowTransitionRepository;
import com.govfix.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStateRepository workflowStateRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final AuditService auditService;

    public WorkflowController(WorkflowDefinitionRepository workflowDefinitionRepository,
                              WorkflowStateRepository workflowStateRepository,
                              WorkflowTransitionRepository workflowTransitionRepository,
                              AuditService auditService) {
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.workflowStateRepository = workflowStateRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowDefinition>>> getAllWorkflows() {
        List<WorkflowDefinition> workflows = workflowDefinitionRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok(workflows));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> getWorkflowById(@PathVariable Long id) {
        WorkflowDefinition workflow = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(workflow));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<WorkflowDefinition>> createWorkflow(
            @Valid @RequestBody WorkflowConfigRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "admin_govfix";
        String role = auth != null && !auth.getAuthorities().isEmpty()
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "ADMIN";

        WorkflowDefinition def = new WorkflowDefinition(
                request.getWorkflowKey(),
                request.getName(),
                request.getDescription(),
                request.getInitialStateKey() != null ? request.getInitialStateKey() : "Submitted",
                request.getCategoryPattern()
        );
        def.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        WorkflowDefinition saved = workflowDefinitionRepository.save(def);

        if (request.getStates() != null) {
            for (WorkflowConfigRequest.StateDto s : request.getStates()) {
                WorkflowState state = new WorkflowState(
                        saved,
                        s.getStateKey(),
                        s.getDisplayName(),
                        s.getStageOrder() != null ? s.getStageOrder() : 0,
                        s.getDeadlineHours() != null ? s.getDeadlineHours() : 24,
                        s.getIsTerminal() != null ? s.getIsTerminal() : false,
                        s.getRequiresEvidence() != null ? s.getRequiresEvidence() : false,
                        s.getColorCode() != null ? s.getColorCode() : "#0d9488"
                );
                workflowStateRepository.save(state);
            }
        }

        if (request.getTransitions() != null) {
            for (WorkflowConfigRequest.TransitionDto t : request.getTransitions()) {
                WorkflowTransition trans = new WorkflowTransition(
                        saved,
                        t.getFromStateKey(),
                        t.getToStateKey(),
                        t.getActionName(),
                        t.getAllowedRoles(),
                        t.getRequiresNotes() != null ? t.getRequiresNotes() : false,
                        t.getRequiresEvidence() != null ? t.getRequiresEvidence() : false,
                        t.getRequiresApproval() != null ? t.getRequiresApproval() : false
                );
                workflowTransitionRepository.save(trans);
            }
        }

        auditService.logAuditEvent(username, role, null, null,
                "WORKFLOW_PUBLISHED", null, null, "SUCCESS",
                "Created and published new workflow: " + saved.getName() + " (" + saved.getWorkflowKey() + ")");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Workflow published successfully", saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<WorkflowDefinition>> updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowConfigRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "admin_govfix";
        String role = auth != null && !auth.getAuthorities().isEmpty()
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "ADMIN";

        WorkflowDefinition def = workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + id));

        def.setName(request.getName());
        def.setDescription(request.getDescription());
        def.setCategoryPattern(request.getCategoryPattern());
        def.setIsActive(request.getIsActive());
        def.setUpdatedAt(LocalDateTime.now());
        WorkflowDefinition saved = workflowDefinitionRepository.save(def);

        auditService.logAuditEvent(username, role, null, null,
                "WORKFLOW_UPDATED", null, null, "SUCCESS",
                "Updated workflow definition: " + saved.getName());

        return ResponseEntity.ok(ApiResponse.ok("Workflow configuration updated", saved));
    }
}
