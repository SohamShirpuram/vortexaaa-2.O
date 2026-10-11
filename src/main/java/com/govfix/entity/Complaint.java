package com.govfix.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true, length = 30)
    private String ticketId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 60)
    private String department;

    @Column(nullable = false, length = 100)
    private String locality;

    @Column(name = "ward_or_zone", length = 50)
    private String wardOrZone;

    @Column(length = 150)
    private String landmark;

    @Column(length = 20)
    private String priority = "MEDIUM";

    @Column(nullable = false, length = 50)
    private String status = "Submitted";

    @Column(name = "citizen_name", nullable = false, length = 100)
    private String citizenName;

    @Column(name = "citizen_contact", length = 50)
    private String citizenContact;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_officer_id")
    @JsonIgnoreProperties({"passwordHash", "createdAt"})
    private User assignedOfficer;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "is_escalated")
    private Boolean isEscalated = false;

    @Column(name = "citizen_feedback_rating")
    private Integer citizenFeedbackRating;

    @Column(name = "citizen_feedback_comments", columnDefinition = "TEXT")
    private String citizenFeedbackComments;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("complaint")
    private List<EvidenceRecord> evidenceList = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("complaint")
    private List<Escalation> escalations = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Complaint() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getWardOrZone() { return wardOrZone; }
    public void setWardOrZone(String wardOrZone) { this.wardOrZone = wardOrZone; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getCitizenContact() { return citizenContact; }
    public void setCitizenContact(String citizenContact) { this.citizenContact = citizenContact; }

    public User getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(User assignedOfficer) { this.assignedOfficer = assignedOfficer; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public Boolean getIsEscalated() { return isEscalated; }
    public void setIsEscalated(Boolean isEscalated) { this.isEscalated = isEscalated; }

    public Integer getCitizenFeedbackRating() { return citizenFeedbackRating; }
    public void setCitizenFeedbackRating(Integer citizenFeedbackRating) { this.citizenFeedbackRating = citizenFeedbackRating; }

    public String getCitizenFeedbackComments() { return citizenFeedbackComments; }
    public void setCitizenFeedbackComments(String citizenFeedbackComments) { this.citizenFeedbackComments = citizenFeedbackComments; }

    public List<EvidenceRecord> getEvidenceList() { return evidenceList; }
    public void setEvidenceList(List<EvidenceRecord> evidenceList) { this.evidenceList = evidenceList; }

    public List<Escalation> getEscalations() { return escalations; }
    public void setEscalations(List<Escalation> escalations) { this.escalations = escalations; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
