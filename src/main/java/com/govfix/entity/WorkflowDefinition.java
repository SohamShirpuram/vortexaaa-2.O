package com.govfix.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_definitions")
public class WorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_key", nullable = false, unique = true, length = 50)
    private String workflowKey;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "initial_state_key", nullable = false, length = 50)
    private String initialStateKey;

    @Column(name = "category_pattern", length = 100)
    private String categoryPattern;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkflowState> states = new ArrayList<>();

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkflowTransition> transitions = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public WorkflowDefinition() {}

    public WorkflowDefinition(String workflowKey, String name, String description, String initialStateKey, String categoryPattern) {
        this.workflowKey = workflowKey;
        this.name = name;
        this.description = description;
        this.initialStateKey = initialStateKey;
        this.categoryPattern = categoryPattern;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public List<WorkflowState> getStates() { return states; }
    public void setStates(List<WorkflowState> states) { this.states = states; }

    public List<WorkflowTransition> getTransitions() { return transitions; }
    public void setTransitions(List<WorkflowTransition> transitions) { this.transitions = transitions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
