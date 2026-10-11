package com.govfix.controller;

import com.govfix.dto.*;
import com.govfix.entity.AuditEvent;
import com.govfix.entity.Complaint;
import com.govfix.entity.EvidenceRecord;
import com.govfix.entity.WorkflowTransition;
import com.govfix.service.ComplaintService;
import com.govfix.service.FileStorageService;
import com.govfix.service.WorkflowEngineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final WorkflowEngineService workflowEngineService;
    private final FileStorageService fileStorageService;

    public ComplaintController(ComplaintService complaintService,
                               WorkflowEngineService workflowEngineService,
                               FileStorageService fileStorageService) {
        this.complaintService = complaintService;
        this.workflowEngineService = workflowEngineService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Complaint>>> getComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String locality) {

        List<Complaint> complaints = complaintService.getAllComplaints(status, category, priority, locality);
        return ResponseEntity.ok(ApiResponse.ok(complaints));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Complaint>> createComplaint(
            @Valid @RequestBody ComplaintCreateRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "citizen_portal";
        String role = getRoleName(auth);

        Complaint created = complaintService.createComplaint(request, username, role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Grievance registered successfully with Ticket ID: " + created.getTicketId(), created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComplaintById(
            @PathVariable Long id) {

        Complaint complaint = complaintService.getComplaintById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + id));

        List<WorkflowTransition> availableTransitions = workflowEngineService.getAvailableTransitions(complaint);

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "complaint", complaint,
                "availableTransitions", availableTransitions
        )));
    }

    @GetMapping("/track/{ticketId}")
    public ResponseEntity<ApiResponse<Complaint>> trackByTicketId(@PathVariable String ticketId) {
        Complaint complaint = complaintService.getComplaintByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        return ResponseEntity.ok(ApiResponse.ok(complaint));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<AuditEvent>>> getTimeline(@PathVariable Long id) {
        List<AuditEvent> timeline = complaintService.getComplaintTimeline(id);
        return ResponseEntity.ok(ApiResponse.ok("Complaint timeline retrieved", timeline));
    }

    @PostMapping("/{id}/transitions")
    public ResponseEntity<ApiResponse<Complaint>> executeTransition(
            @PathVariable Long id,
            @Valid @RequestBody TransitionRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "anonymous";
        String role = getRoleName(auth);

        Complaint updated = workflowEngineService.executeTransition(id, request, username, role);
        return ResponseEntity.ok(ApiResponse.ok("Transition successful to state: " + updated.getStatus(), updated));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<Complaint>> assignOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AssignRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "supervisor";
        String role = getRoleName(auth);

        Complaint assigned = complaintService.assignOfficer(id, request, username, role);
        return ResponseEntity.ok(ApiResponse.ok("Complaint successfully assigned to officer", assigned));
    }

    @PostMapping("/{id}/inspection")
    public ResponseEntity<ApiResponse<EvidenceRecord>> recordInspection(
            @PathVariable Long id,
            @Valid @RequestBody InspectionRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "officer";
        String role = getRoleName(auth);

        EvidenceRecord inspection = complaintService.recordInspection(id, request, username, role);
        return ResponseEntity.ok(ApiResponse.ok("Inspection record saved", inspection));
    }

    @PostMapping("/{id}/evidence")
    public ResponseEntity<ApiResponse<EvidenceRecord>> uploadEvidence(
            @PathVariable Long id,
            @RequestParam("evidenceType") String evidenceType,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "fileUrl", required = false) String fileUrl,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "officer";
        String role = getRoleName(auth);

        String storedPath;
        String fileName;
        long fileSize = 200000L;
        String mimeType = "image/jpeg";

        if (file != null && !file.isEmpty()) {
            storedPath = fileStorageService.storeFile(file);
            fileName = file.getOriginalFilename();
            fileSize = file.getSize();
            mimeType = file.getContentType();
        } else if (fileUrl != null && !fileUrl.isBlank()) {
            storedPath = fileUrl;
            fileName = "demo_attached_evidence.jpg";
        } else {
            throw new IllegalArgumentException("Either a file upload or evidence URL is required");
        }

        EvidenceRecord saved = complaintService.addEvidence(
                id, evidenceType, fileName, storedPath, fileSize, mimeType, notes, username, role
        );

        return ResponseEntity.ok(ApiResponse.ok("Evidence attached successfully", saved));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<Complaint>> submitFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "citizen";
        String role = getRoleName(auth);

        Complaint updated = complaintService.submitFeedback(id, request, username, role);
        return ResponseEntity.ok(ApiResponse.ok("Citizen feedback recorded. Thank you!", updated));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<Complaint>> requestReopen(
            @PathVariable Long id,
            @Valid @RequestBody ReopenRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "citizen";
        String role = getRoleName(auth);

        Complaint reopened = complaintService.requestReopen(id, request, username, role);
        return ResponseEntity.ok(ApiResponse.ok("Reopen request accepted. Ticket status updated to Reopened.", reopened));
    }

    private String getRoleName(Authentication auth) {
        if (auth == null || auth.getAuthorities().isEmpty()) return "CITIZEN";
        return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }
}
