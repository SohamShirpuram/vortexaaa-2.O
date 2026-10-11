package com.govfix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_transitions")
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    @JsonIgnore
    private WorkflowDefinition workflow;

    @Column(name = "from_state_key", nullable = false, length = 50)
    private String fromStateKey;

    @Column(name = "to_state_key", nullable = false, length = 50)
    private String toStateKey;

    @Column(name = "action_name", nullable = false, length = 100)
    private String actionName;

    @Column(name = "allowed_roles", nullable = false, length = 150)
    private String allowedRoles; // Comma separated: OFFICER,SUPERVISOR,ADMIN

    @Column(name = "requires_notes")
    private Boolean requiresNotes = false;

    @Column(name = "requires_evidence")
    private Boolean requiresEvidence = false;

    @Column(name = "requires_approval")
    private Boolean requiresApproval = false;

    public WorkflowTransition() {}

    public WorkflowTransition(WorkflowDefinition workflow, String fromStateKey, String toStateKey, String actionName, String allowedRoles, Boolean requiresNotes, Boolean requiresEvidence, Boolean requiresApproval) {
        this.workflow = workflow;
        this.fromStateKey = fromStateKey;
        this.toStateKey = toStateKey;
        this.actionName = actionName;
        this.allowedRoles = allowedRoles;
        this.requiresNotes = requiresNotes;
        this.requiresEvidence = requiresEvidence;
        this.requiresApproval = requiresApproval;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkflowDefinition getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowDefinition workflow) { this.workflow = workflow; }

    public String getFromStateKey() { return fromStateKey; }
    public void setFromStateKey(String fromStateKey) { this.fromStateKey = fromStateKey; }

    public String getToStateKey() { return toStateKey; }
    public void setToStateKey(String toStateKey) { this.toStateKey = toStateKey; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public String getAllowedRoles() { return allowedRoles; }
    public void setAllowedRoles(String allowedRoles) { this.allowedRoles = allowedRoles; }

    public Boolean getRequiresNotes() { return requiresNotes; }
    public void setRequiresNotes(Boolean requiresNotes) { this.requiresNotes = requiresNotes; }

    public Boolean getRequiresEvidence() { return requiresEvidence; }
    public void setRequiresEvidence(Boolean requiresEvidence) { this.requiresEvidence = requiresEvidence; }

    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }

    public boolean isRoleAllowed(String roleName) {
        if (allowedRoles == null) return false;
        String cleanRole = roleName.replace("ROLE_", "");
        for (String r : allowedRoles.split(",")) {
            if (r.trim().equalsIgnoreCase(cleanRole)) {
                return true;
            }
        }
        return false;
    }
}
