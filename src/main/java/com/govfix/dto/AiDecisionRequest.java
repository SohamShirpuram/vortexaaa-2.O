package com.govfix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AiDecisionRequest {

    @NotNull(message = "Recommendation ID is required")
    private Long recommendationId;

    @NotBlank(message = "Decision is required: APPROVE, REJECT, or EDIT")
    private String decision; // APPROVE, REJECT, EDIT

    private String editedCategory;
    private String editedDepartment;
    private String editedPriority;
    private String notes;

    public AiDecisionRequest() {}

    public Long getRecommendationId() { return recommendationId; }
    public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getEditedCategory() { return editedCategory; }
    public void setEditedCategory(String editedCategory) { this.editedCategory = editedCategory; }

    public String getEditedDepartment() { return editedDepartment; }
    public void setEditedDepartment(String editedDepartment) { this.editedDepartment = editedDepartment; }

    public String getEditedPriority() { return editedPriority; }
    public void setEditedPriority(String editedPriority) { this.editedPriority = editedPriority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

