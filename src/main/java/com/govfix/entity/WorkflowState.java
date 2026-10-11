package com.govfix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_states")
public class WorkflowState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    @JsonIgnore
    private WorkflowDefinition workflow;

    @Column(name = "state_key", nullable = false, length = 50)
    private String stateKey;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "stage_order")
    private Integer stageOrder = 0;

    @Column(name = "deadline_hours")
    private Integer deadlineHours = 48;

    @Column(name = "is_terminal")
    private Boolean isTerminal = false;

    @Column(name = "requires_evidence")
    private Boolean requiresEvidence = false;

    @Column(name = "color_code", length = 20)
    private String colorCode = "#0d9488";

    public WorkflowState() {}

    public WorkflowState(WorkflowDefinition workflow, String stateKey, String displayName, Integer stageOrder, Integer deadlineHours, Boolean isTerminal, Boolean requiresEvidence, String colorCode) {
        this.workflow = workflow;
        this.stateKey = stateKey;
        this.displayName = displayName;
        this.stageOrder = stageOrder;
        this.deadlineHours = deadlineHours;
        this.isTerminal = isTerminal;
        this.requiresEvidence = requiresEvidence;
        this.colorCode = colorCode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkflowDefinition getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowDefinition workflow) { this.workflow = workflow; }

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

