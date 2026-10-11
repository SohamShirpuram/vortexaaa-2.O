package com.govfix;

import com.govfix.dto.*;
import com.govfix.entity.*;
import com.govfix.repository.*;
import com.govfix.service.ComplaintService;
import com.govfix.service.WorkflowEngineService;
import com.govfix.workflow.WorkflowException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WorkflowEngineTests {

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    @Test
    @DisplayName("Verify complaint registration generates ticket ID and initial state")
    public void testComplaintRegistration() {
        ComplaintCreateRequest request = new ComplaintCreateRequest();
        request.setTitle("Pothole near Nigdi Bus Stand");
        request.setDescription("Severe depression on the road surface causing traffic delay.");
        request.setCategory("Roads and potholes");
        request.setLocality("Nigdi");
        request.setCitizenName("Anita Sharma");
        request.setCitizenContact("9822123456");

        Complaint created = complaintService.createComplaint(request, "anita_sharma", "CITIZEN");

        assertNotNull(created.getId());
        assertNotNull(created.getTicketId());
        assertTrue(created.getTicketId().startsWith("GV-"));
        assertEquals("Submitted", created.getStatus());
        assertEquals("Roads and Infrastructure", created.getDepartment());
        assertNotNull(created.getDeadlineAt());
    }

    @Test
    @DisplayName("Verify valid transition succeeds from Submitted to Under Review")
    public void testValidTransition() {
        ComplaintCreateRequest request = new ComplaintCreateRequest();
        request.setTitle("Drain overflow near Chinchwad station");
        request.setDescription("Water clogging on pedestrian footpath.");
        request.setCategory("Roads and potholes");
        request.setLocality("Chinchwad");
        Complaint c = complaintService.createComplaint(request, "citizen_rahul", "CITIZEN");

        TransitionRequest trans = new TransitionRequest("Under Review", "Field engineer review initiated");
        Complaint updated = workflowEngineService.executeTransition(c.getId(), trans, "officer_sharma", "OFFICER");

        assertEquals("Under Review", updated.getStatus());
    }

    @Test
    @DisplayName("Verify invalid transition is rejected and logged in audit log")
    public void testInvalidTransitionRejectionAndAudit() {
        ComplaintCreateRequest request = new ComplaintCreateRequest();
        request.setTitle("Illegal skip transition test");
        request.setDescription("Attempting to jump directly from Submitted to Resolved");
        request.setCategory("Roads and potholes");
        request.setLocality("Akurdi");
        Complaint c = complaintService.createComplaint(request, "citizen_rahul", "CITIZEN");

        long auditCountBefore = auditEventRepository.count();

        // Attempt illegal jump from Submitted to Resolved
        TransitionRequest illegalTrans = new TransitionRequest("Resolved", "Attempting illegal direct jump");
        assertThrows(WorkflowException.class, () -> {
            workflowEngineService.executeTransition(c.getId(), illegalTrans, "officer_sharma", "OFFICER");
        });

        // Ensure state was NOT changed
        Complaint fresh = complaintRepository.findById(c.getId()).orElseThrow();
        assertEquals("Submitted", fresh.getStatus());

        // Ensure rejected attempt survived in audit log
        long auditCountAfter = auditEventRepository.count();
        assertTrue(auditCountAfter > auditCountBefore);

        List<AuditEvent> auditLogs = auditEventRepository.findByTicketIdOrderByTimestampDesc(c.getTicketId());
        boolean hasRejectedAudit = auditLogs.stream().anyMatch(a -> "REJECTED".equalsIgnoreCase(a.getOutcome()));
        assertTrue(hasRejectedAudit, "Rejected transition attempt must be recorded in audit log");
    }

    @Test
    @DisplayName("Verify unauthorized role is rejected")
    public void testUnauthorizedRoleRejection() {
        ComplaintCreateRequest request = new ComplaintCreateRequest();
        request.setTitle("Role permission test");
        request.setDescription("Citizen attempting to assign officer");
        request.setCategory("Roads and potholes");
        request.setLocality("Ravet");
        Complaint c = complaintService.createComplaint(request, "citizen_rahul", "CITIZEN");

        // First transition to Under Review
        workflowEngineService.executeTransition(c.getId(), new TransitionRequest("Under Review", "Reviewed"), "officer_sharma", "OFFICER");

        // Citizen attempts to transition to Assigned (requires SUPERVISOR or ADMIN)
        TransitionRequest assignAttempt = new TransitionRequest("Assigned", "Citizen trying to assign");
        assertThrows(WorkflowException.class, () -> {
            workflowEngineService.executeTransition(c.getId(), assignAttempt, "citizen_rahul", "CITIZEN");
        });

        Complaint fresh = complaintRepository.findById(c.getId()).orElseThrow();
        assertEquals("Under Review", fresh.getStatus());
    }

    @Test
    @DisplayName("Verify evidence requirement enforcement")
    public void testEvidenceRequirementEnforced() {
        ComplaintCreateRequest request = new ComplaintCreateRequest();
        request.setTitle("Evidence check pothole");
        request.setDescription("Pothole needing mandatory inspection photo before progress");
        request.setCategory("Roads and potholes");
        request.setLocality("Wakad");
        Complaint c = complaintService.createComplaint(request, "citizen_rahul", "CITIZEN");

        // Submitted -> Under Review -> Assigned -> Inspection Required
        workflowEngineService.executeTransition(c.getId(), new TransitionRequest("Under Review", "Review"), "officer_sharma", "OFFICER");
        workflowEngineService.executeTransition(c.getId(), new TransitionRequest("Assigned", "Assigning to Amit"), "supervisor_kulkarni", "SUPERVISOR");
        workflowEngineService.executeTransition(c.getId(), new TransitionRequest("Inspection Required", "Inspect site"), "officer_sharma", "OFFICER");

        // Attempt transition to In Progress without recording evidence
        TransitionRequest toInProgressNoEvidence = new TransitionRequest("In Progress", "Work starting without photo");
        assertThrows(WorkflowException.class, () -> {
            workflowEngineService.executeTransition(c.getId(), toInProgressNoEvidence, "officer_sharma", "OFFICER");
        });

        // Now record evidence and transition again - should succeed!
        complaintService.addEvidence(c.getId(), "BEFORE_PHOTO", "site_before.jpg", "https://example.com/site.jpg", 1000L, "image/jpeg", "Pre-repair photo", "officer_sharma", "OFFICER");

        TransitionRequest toInProgressWithEvidence = new TransitionRequest("In Progress", "Work commencing after site photo");
        Complaint updated = workflowEngineService.executeTransition(c.getId(), toInProgressWithEvidence, "officer_sharma", "OFFICER");
        assertEquals("In Progress", updated.getStatus());
    }
}

