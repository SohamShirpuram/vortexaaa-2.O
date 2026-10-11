package com.govfix.dto;

import jakarta.validation.constraints.NotBlank;

public class InspectionRequest {

    @NotBlank(message = "Inspection notes are required")
    private String notes;

    private String evidenceUrl;
    private String evidenceType = "INSPECTION_REPORT";

    public InspectionRequest() {}

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }
}

