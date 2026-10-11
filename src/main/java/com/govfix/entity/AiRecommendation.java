package com.govfix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_recommendations")
public class AiRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;

    @Column(name = "suggested_category", nullable = false, length = 60)
    private String suggestedCategory;

    @Column(name = "suggested_department", nullable = false, length = 60)
    private String suggestedDepartment;

    @Column(name = "suggested_priority", nullable = false, length = 20)
    private String suggestedPriority;

    @Column(name = "confidence_score")
    private Double confidenceScore = 0.85;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, EDITED

    @Column(name = "decided_by_username", length = 50)
    private String decidedByUsername;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public AiRecommendation() {}

    public AiRecommendation(Complaint complaint, String suggestedCategory, String suggestedDepartment, String suggestedPriority, Double confidenceScore, String explanation) {
        this.complaint = complaint;
        this.suggestedCategory = suggestedCategory;
        this.suggestedDepartment = suggestedDepartment;
        this.suggestedPriority = suggestedPriority;
        this.confidenceScore = confidenceScore;
        this.explanation = explanation;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public String getSuggestedCategory() { return suggestedCategory; }
    public void setSuggestedCategory(String suggestedCategory) { this.suggestedCategory = suggestedCategory; }

    public String getSuggestedDepartment() { return suggestedDepartment; }
    public void setSuggestedDepartment(String suggestedDepartment) { this.suggestedDepartment = suggestedDepartment; }

    public String getSuggestedPriority() { return suggestedPriority; }
    public void setSuggestedPriority(String suggestedPriority) { this.suggestedPriority = suggestedPriority; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDecidedByUsername() { return decidedByUsername; }
    public void setDecidedByUsername(String decidedByUsername) { this.decidedByUsername = decidedByUsername; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
