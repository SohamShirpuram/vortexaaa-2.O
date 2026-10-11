package com.govfix.dto;

import jakarta.validation.constraints.NotBlank;

public class TransitionRequest {

    @NotBlank(message = "Target state key is required")
    private String targetState;

    private String notes;
    private String evidenceUrl;
    private String evidenceType; // BEFORE_PHOTO, AFTER_PHOTO, INSPECTION_REPORT

    public TransitionRequest() {}

    public TransitionRequest(String targetState, String notes) {
        this.targetState = targetState;
        this.notes = notes;
    }

    public String getTargetState() { return targetState; }
    public void setTargetState(String targetState) { this.targetState = targetState; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }
}

