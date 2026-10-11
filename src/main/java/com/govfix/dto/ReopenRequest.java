package com.govfix.dto;

import jakarta.validation.constraints.NotBlank;

public class ReopenRequest {

    @NotBlank(message = "Reason for reopening is required")
    private String reason;

    public ReopenRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

