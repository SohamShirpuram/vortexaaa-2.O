package com.govfix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evidence_records")
public class EvidenceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "officer_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "createdAt"})
    private User officer;

    @Column(name = "evidence_type", nullable = false, length = 50)
    private String evidenceType; // BEFORE_PHOTO, AFTER_PHOTO, INSPECTION_REPORT

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public EvidenceRecord() {}

    public EvidenceRecord(Complaint complaint, User officer, String evidenceType, String fileName, String filePath, Long fileSize, String mimeType, String notes) {
        this.complaint = complaint;
        this.officer = officer;
        this.evidenceType = evidenceType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }

    public User getOfficer() { return officer; }
    public void setOfficer(User officer) { this.officer = officer; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
