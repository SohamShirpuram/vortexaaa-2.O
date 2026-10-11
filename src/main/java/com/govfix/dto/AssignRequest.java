package com.govfix.dto;

import jakarta.validation.constraints.NotNull;

public class AssignRequest {

    @NotNull(message = "Officer ID is required")
    private Long officerId;

    private String notes;

    public AssignRequest() {}

    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

