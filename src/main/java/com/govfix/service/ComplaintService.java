package com.govfix.service;

import com.govfix.dto.*;
import com.govfix.entity.*;
import com.govfix.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintService.class);

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final AuditEventRepository auditEventRepository;
    private final AuditService auditService;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            EvidenceRecordRepository evidenceRecordRepository,
                            AuditEventRepository auditEventRepository,
                            AuditService auditService,
                            WorkflowDefinitionRepository workflowDefinitionRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.auditEventRepository = auditEventRepository;
        this.auditService = auditService;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
    }

    @Transactional
    public Complaint createComplaint(ComplaintCreateRequest request, String actorUsername, String actorRole) {
        Complaint complaint = new Complaint();

        // Generate unique Ticket ID: GV-1000 + sequence or count
        long count = complaintRepository.count();
        complaint.setTicketId("GV-" + (1001 + count));

        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setCategory(request.getCategory());
        complaint.setDepartment(resolveDepartmentForCategory(request.getCategory()));
        complaint.setLocality(request.getLocality());
        complaint.setWardOrZone(request.getWardOrZone() != null ? request.getWardOrZone() : "Ward 14 (PCMC Zone A)");
        complaint.setLandmark(request.getLandmark());
        complaint.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        complaint.setStatus("Submitted");
        complaint.setCitizenName(request.getCitizenName() != null ? request.getCitizenName() : "Citizen User");
        complaint.setCitizenContact(request.getCitizenContact() != null ? request.getCitizenContact() : "9822000000");
        complaint.setPhotoUrl(request.getPhotoUrl());

        // Set initial 24h SLA deadline
        complaint.setDeadlineAt(LocalDateTime.now().plusHours(24));
        complaint.setIsEscalated(false);

        Complaint saved = complaintRepository.save(complaint);

        auditService.logAuditEvent(
                actorUsername != null ? actorUsername : "citizen_portal",
                actorRole != null ? actorRole : "CITIZEN",
                saved.getId(),
                saved.getTicketId(),
                "COMPLAINT_REGISTERED",
                null,
                "Submitted",
                "SUCCESS",
                "Grievance filed: " + saved.getTitle() + " in locality " + saved.getLocality()
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Complaint> getAllComplaints(String status, String category, String priority, String locality) {
        return complaintRepository.searchComplaints(status, category, priority, locality);
    }

    @Transactional(readOnly = true)
    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Complaint> getComplaintByTicketId(String ticketId) {
        return complaintRepository.findByTicketId(ticketId);
    }

    @Transactional
    public Complaint assignOfficer(Long complaintId, AssignRequest request, String actorUsername, String actorRole) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        User officer = userRepository.findById(request.getOfficerId())
                .orElseThrow(() -> new IllegalArgumentException("Officer not found: " + request.getOfficerId()));

        complaint.setAssignedOfficer(officer);
        complaint.setStatus("Assigned");
        complaint.setUpdatedAt(LocalDateTime.now());
        Complaint saved = complaintRepository.save(complaint);

        auditService.logAuditEvent(actorUsername, actorRole, saved.getId(), saved.getTicketId(),
                "ASSIGN_OFFICER", "Under Review", "Assigned", "SUCCESS",
                "Assigned to " + officer.getFullName() + " (" + officer.getDepartment() + ")" +
                (request.getNotes() != null ? ". Notes: " + request.getNotes() : ""));

        return saved;
    }

    @Transactional
    public EvidenceRecord recordInspection(Long complaintId, InspectionRequest request, String actorUsername, String actorRole) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        User officer = userRepository.findByUsername(actorUsername)
                .orElse(complaint.getAssignedOfficer() != null ? complaint.getAssignedOfficer() : userRepository.findAll().stream().filter(u -> u.getRole() == Role.OFFICER).findFirst().orElse(null));

        EvidenceRecord evidence = new EvidenceRecord();
        evidence.setComplaint(complaint);
        evidence.setOfficer(officer);
        evidence.setEvidenceType(request.getEvidenceType() != null ? request.getEvidenceType() : "INSPECTION_REPORT");
        evidence.setFileName("inspection_record_" + System.currentTimeMillis() + ".pdf");
        evidence.setFilePath(request.getEvidenceUrl() != null ? request.getEvidenceUrl() : "/uploads/inspection_report.pdf");
        evidence.setFileSize(204800L);
        evidence.setMimeType("application/pdf");
        evidence.setNotes(request.getNotes());

        EvidenceRecord saved = evidenceRecordRepository.save(evidence);

        auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                "INSPECTION_RECORDED", complaint.getStatus(), complaint.getStatus(), "SUCCESS",
                "Site inspection recorded: " + request.getNotes());

        return saved;
    }

    @Transactional
    public EvidenceRecord addEvidence(Long complaintId, String evidenceType, String fileName, String filePath, Long fileSize, String mimeType, String notes, String actorUsername, String actorRole) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        User officer = userRepository.findByUsername(actorUsername)
                .orElse(complaint.getAssignedOfficer() != null ? complaint.getAssignedOfficer() : userRepository.findAll().stream().filter(u -> u.getRole() == Role.OFFICER).findFirst().orElse(null));

        EvidenceRecord record = new EvidenceRecord(complaint, officer, evidenceType, fileName, filePath, fileSize, mimeType, notes);
        EvidenceRecord saved = evidenceRecordRepository.save(record);

        auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                "EVIDENCE_UPLOADED", complaint.getStatus(), complaint.getStatus(), "SUCCESS",
                "Evidence type '" + evidenceType + "' uploaded: " + fileName + (notes != null ? " (" + notes + ")" : ""));

        return saved;
    }

    @Transactional
    public Complaint submitFeedback(Long complaintId, FeedbackRequest request, String actorUsername, String actorRole) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        complaint.setCitizenFeedbackRating(request.getRating());
        complaint.setCitizenFeedbackComments(request.getComments());
        complaint.setUpdatedAt(LocalDateTime.now());
        Complaint saved = complaintRepository.save(complaint);

        auditService.logAuditEvent(actorUsername, actorRole, saved.getId(), saved.getTicketId(),
                "CITIZEN_FEEDBACK", saved.getStatus(), saved.getStatus(), "SUCCESS",
                "Citizen rated: " + request.getRating() + "/5 stars. Comments: " + request.getComments());

        return saved;
    }

    @Transactional
    public Complaint requestReopen(Long complaintId, ReopenRequest request, String actorUsername, String actorRole) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        String prevState = complaint.getStatus();
        complaint.setStatus("Reopened");
        complaint.setResolvedAt(null);
        complaint.setDeadlineAt(LocalDateTime.now().plusHours(24));
        complaint.setUpdatedAt(LocalDateTime.now());
        Complaint saved = complaintRepository.save(complaint);

        auditService.logAuditEvent(actorUsername, actorRole, saved.getId(), saved.getTicketId(),
                "CITIZEN_REOPEN", prevState, "Reopened", "SUCCESS",
                "Citizen requested case reopening: " + request.getReason());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getComplaintTimeline(Long complaintId) {
        return auditEventRepository.findByComplaintIdOrderByTimestampDesc(complaintId);
    }

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalComplaints(complaintRepository.count());
        stats.setPendingComplaints(complaintRepository.countByStatus("Submitted") + complaintRepository.countByStatus("Under Review"));
        stats.setAssignedComplaints(complaintRepository.countByStatus("Assigned") + complaintRepository.countByStatus("Inspection Required"));
        stats.setInProgressComplaints(complaintRepository.countByStatus("In Progress") + complaintRepository.countByStatus("Resolution Submitted"));
        stats.setResolvedComplaints(complaintRepository.countByStatus("Resolved"));
        stats.setClosedComplaints(complaintRepository.countByStatus("Closed"));
        stats.setOverdueComplaints(complaintRepository.countOverdue(LocalDateTime.now()));
        stats.setEscalatedComplaints(complaintRepository.countEscalated());

        // Category breakdown
        Map<String, Long> catMap = new LinkedHashMap<>();
        for (String cat : List.of("Roads and potholes", "Water supply", "Drainage and sanitation", "Streetlights", "Waste management", "Public gardens")) {
            long count = complaintRepository.findAll().stream().filter(c -> cat.equalsIgnoreCase(c.getCategory())).count();
            catMap.put(cat, count);
        }
        stats.setCategoryBreakdown(catMap);

        // Status breakdown
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (String st : List.of("Submitted", "Under Review", "Assigned", "Inspection Required", "In Progress", "Resolution Submitted", "Resolved", "Closed", "Escalated", "Reopened")) {
            statusMap.put(st, complaintRepository.countByStatus(st));
        }
        stats.setStatusBreakdown(statusMap);

        return stats;
    }

    private String resolveDepartmentForCategory(String category) {
        if (category == null) return "Public Works";
        switch (category.toLowerCase()) {
            case "roads and potholes": return "Roads and Infrastructure";
            case "water supply": return "Water Supply & Quality";
            case "drainage and sanitation": return "Drainage Department";
            case "streetlights": return "Electrical Department";
            case "waste management": return "Waste Management";
            case "public gardens": return "Garden Department";
            default: return "Municipal Civic Services";
        }
    }
}
