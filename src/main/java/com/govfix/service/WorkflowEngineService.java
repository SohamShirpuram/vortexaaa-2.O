package com.govfix.service;

import com.govfix.dto.TransitionRequest;
import com.govfix.entity.*;
import com.govfix.repository.*;
import com.govfix.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkflowEngineService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

    private final ComplaintRepository complaintRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStateRepository workflowStateRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final AuditService auditService;

    public WorkflowEngineService(ComplaintRepository complaintRepository,
                                 WorkflowDefinitionRepository workflowDefinitionRepository,
                                 WorkflowStateRepository workflowStateRepository,
                                 WorkflowTransitionRepository workflowTransitionRepository,
                                 EvidenceRecordRepository evidenceRecordRepository,
                                 AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.workflowStateRepository = workflowStateRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.auditService = auditService;
    }

    /**
     * Executes a state transition on a complaint with full backend validation.
     * Rejections are logged to the independent audit log boundary and thrown as WorkflowException.
     */
    @Transactional
    public Complaint executeTransition(Long complaintId, TransitionRequest request, String actorUsername, String actorRole) {
        // Validation 1: Complaint exists
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> {
                    String reason = "Complaint ID " + complaintId + " does not exist";
                    auditService.logAuditEvent(actorUsername, actorRole, complaintId, null,
                            "TRANSITION_ATTEMPT", null, request.getTargetState(), "REJECTED", reason);
                    return new WorkflowException(reason);
                });

        String currentState = complaint.getStatus();
        String requestedState = request.getTargetState();

        // Resolve workflow configuration for this complaint category
        WorkflowDefinition workflow = resolveWorkflowForComplaint(complaint);
        if (workflow == null) {
            String reason = "No active workflow definition configured for category: " + complaint.getCategory();
            auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                    "TRANSITION_ATTEMPT", currentState, requestedState, "REJECTED", reason);
            throw new WorkflowException(reason);
        }

        // Validation 2 & 3: Requested transition is configured and matches current state
        Optional<WorkflowTransition> transitionOpt = workflowTransitionRepository
                .findByWorkflowIdAndFromStateKeyAndToStateKey(workflow.getId(), currentState, requestedState);

        if (transitionOpt.isEmpty()) {
            String reason = String.format("Illegal transition: No configured path from '%s' to '%s' in workflow '%s'",
                    currentState, requestedState, workflow.getName());
            auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                    "TRANSITION_ATTEMPT", currentState, requestedState, "REJECTED", reason);
            throw new WorkflowException(reason);
        }

        WorkflowTransition transition = transitionOpt.get();

        // Validation 4: The user's role is authorized
        if (!transition.isRoleAllowed(actorRole)) {
            String reason = String.format("Unauthorized: Role '%s' is not permitted to execute '%s' (Allowed: %s)",
                    actorRole, transition.getActionName(), transition.getAllowedRoles());
            auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                    "TRANSITION_ATTEMPT", currentState, requestedState, "REJECTED", reason);
            throw new WorkflowException(reason);
        }

        // Validation 5: Required fields, notes, and evidence
        if (Boolean.TRUE.equals(transition.getRequiresNotes()) && (request.getNotes() == null || request.getNotes().trim().isEmpty())) {
            String reason = String.format("Mandatory notes missing for action '%s'", transition.getActionName());
            auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                    "TRANSITION_ATTEMPT", currentState, requestedState, "REJECTED", reason);
            throw new WorkflowException(reason);
        }

        if (Boolean.TRUE.equals(transition.getRequiresEvidence())) {
            long evidenceCount = evidenceRecordRepository.countByComplaintId(complaint.getId());
            boolean hasInlineEvidence = request.getEvidenceUrl() != null && !request.getEvidenceUrl().trim().isEmpty();

            if (evidenceCount == 0 && !hasInlineEvidence) {
                String reason = String.format("Mandatory photographic/inspection evidence missing for action '%s'", transition.getActionName());
                auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                        "TRANSITION_ATTEMPT", currentState, requestedState, "REJECTED", reason);
                throw new WorkflowException(reason);
            }
        }

        // Check if destination state requires evidence
        Optional<WorkflowState> targetStateOpt = workflowStateRepository
                .findByWorkflowIdAndStateKey(workflow.getId(), requestedState);
        if (targetStateOpt.isPresent() && Boolean.TRUE.equals(targetStateOpt.get().getRequiresEvidence())) {
            long evidenceCount = evidenceRecordRepository.countByComplaintId(complaint.getId());
            boolean hasInlineEvidence = request.getEvidenceUrl() != null && !request.getEvidenceUrl().trim().isEmpty();
            if (evidenceCount == 0 && !hasInlineEvidence) {
                String reason = String.format("Target state '%s' strictly requires recorded evidence before entry", requestedState);
                auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                        "TRANSITION_ATTEMPT", currentState, requestedState, "REJECTED", reason);
                throw new WorkflowException(reason);
            }
        }

        // Validation 6: Required approval
        if (Boolean.TRUE.equals(transition.getRequiresApproval())) {
            String cleanRole = actorRole.replace("ROLE_", "");
            if (!cleanRole.equalsIgnoreCase("SUPERVISOR") && !cleanRole.equalsIgnoreCase("ADMIN")) {
                String reason = String.format("Action '%s' requires SUPERVISOR or ADMIN approval signoff", transition.getActionName());
                auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                        "TRANSITION_ATTEMPT", currentState, requestedState, "REJECTED", reason);
                throw new WorkflowException(reason);
            }
        }

        // All 6 validations passed! Update complaint state
        complaint.setStatus(requestedState);
        complaint.setUpdatedAt(LocalDateTime.now());

        // Update lifecycle timestamps
        if ("Resolved".equalsIgnoreCase(requestedState)) {
            complaint.setResolvedAt(LocalDateTime.now());
            complaint.setIsEscalated(false);
        } else if ("Closed".equalsIgnoreCase(requestedState)) {
            complaint.setClosedAt(LocalDateTime.now());
        }

        // Update stage deadline based on target state configuration
        if (targetStateOpt.isPresent()) {
            Integer hours = targetStateOpt.get().getDeadlineHours();
            if (hours != null && hours > 0) {
                complaint.setDeadlineAt(LocalDateTime.now().plusHours(hours));
            }
        }

        Complaint saved = complaintRepository.save(complaint);

        // Record SUCCESS in audit trail
        String successReason = String.format("Transitioned from '%s' to '%s' via action '%s'%s",
                currentState, requestedState, transition.getActionName(),
                (request.getNotes() != null && !request.getNotes().isEmpty() ? " | Notes: " + request.getNotes() : ""));

        auditService.logAuditEvent(actorUsername, actorRole, saved.getId(), saved.getTicketId(),
                "TRANSITION_SUCCESS", currentState, requestedState, "SUCCESS", successReason);

        return saved;
    }

    public List<WorkflowTransition> getAvailableTransitions(Complaint complaint) {
        WorkflowDefinition workflow = resolveWorkflowForComplaint(complaint);
        if (workflow == null) return List.of();
        return workflowTransitionRepository.findByWorkflowIdAndFromStateKey(workflow.getId(), complaint.getStatus());
    }

    public WorkflowDefinition resolveWorkflowForComplaint(Complaint complaint) {
        // Try match by category pattern
        Optional<WorkflowDefinition> match = workflowDefinitionRepository
                .findFirstByCategoryPatternContainingIgnoreCase(complaint.getCategory());
        if (match.isPresent()) {
            return match.get();
        }
        // Fallback to default pothole workflow
        return workflowDefinitionRepository.findByWorkflowKey("pothole_workflow")
                .or(() -> workflowDefinitionRepository.findAll().stream().findFirst())
                .orElse(null);
    }
}

