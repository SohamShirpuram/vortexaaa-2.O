package com.govfix.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class WorkflowConfigRequest {

    @NotBlank(message = "Workflow key is required")
    private String workflowKey;

    @NotBlank(message = "Workflow name is required")
    private String name;

    private String description;
    private String initialStateKey;
    private String categoryPattern;
    private Boolean isActive = true;

    private List<StateDto> states;
    private List<TransitionDto> transitions;

    public static class StateDto {
        private String stateKey;
        private String displayName;
        private Integer stageOrder;
        private Integer deadlineHours;
        private Boolean isTerminal;
        private Boolean requiresEvidence;
        private String colorCode;

        public String getStateKey() { return stateKey; }
        public void setStateKey(String stateKey) { this.stateKey = stateKey; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public Integer getStageOrder() { return stageOrder; }
        public void setStageOrder(Integer stageOrder) { this.stageOrder = stageOrder; }
        public Integer getDeadlineHours() { return deadlineHours; }
        public void setDeadlineHours(Integer deadlineHours) { this.deadlineHours = deadlineHours; }
        public Boolean getIsTerminal() { return isTerminal; }
        public void setIsTerminal(Boolean isTerminal) { this.isTerminal = isTerminal; }
        public Boolean getRequiresEvidence() { return requiresEvidence; }
        public void setRequiresEvidence(Boolean requiresEvidence) { this.requiresEvidence = requiresEvidence; }
        public String getColorCode() { return colorCode; }
        public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    }

    public static class TransitionDto {
        private String fromStateKey;
        private String toStateKey;
        private String actionName;
        private String allowedRoles;
        private Boolean requiresNotes;
        private Boolean requiresEvidence;
        private Boolean requiresApproval;

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
    }

    public WorkflowConfigRequest() {}

    public String getWorkflowKey() { return workflowKey; }
    public void setWorkflowKey(String workflowKey) { this.workflowKey = workflowKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInitialStateKey() { return initialStateKey; }
    public void setInitialStateKey(String initialStateKey) { this.initialStateKey = initialStateKey; }

    public String getCategoryPattern() { return categoryPattern; }
    public void setCategoryPattern(String categoryPattern) { this.categoryPattern = categoryPattern; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public List<StateDto> getStates() { return states; }
    public void setStates(List<StateDto> states) { this.states = states; }

    public List<TransitionDto> getTransitions() { return transitions; }
    public void setTransitions(List<TransitionDto> transitions) { this.transitions = transitions; }
}

