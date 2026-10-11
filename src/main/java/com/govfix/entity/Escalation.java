package com.govfix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "escalations")
public class Escalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;

    @Column(name = "escalation_level")
    private Integer escalationLevel = 1;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "triggered_by", nullable = false, length = 50)
    private String triggeredBy; // SYSTEM_DEADLINE, SUPERVISOR_MANUAL

    @Column(name = "target_role", nullable = false, length = 50)
    private String targetRole; // SUPERVISOR, ADMIN

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public Escalation() {}

    public Escalation(Complaint complaint, Integer escalationLevel, String reason, String triggeredBy, String targetRole) {
        this.complaint = complaint;
        this.escalationLevel = escalationLevel;
        this.reason = reason;
        this.triggeredBy = triggeredBy;
        this.targetRole = targetRole;
        this.isResolved = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public Integer getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
