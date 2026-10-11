package com.govfix;

import com.govfix.dto.*;
import com.govfix.entity.*;
import com.govfix.repository.*;
import com.govfix.service.AIRecommendationService;
import com.govfix.service.ComplaintService;
import com.govfix.service.EscalationService;
import com.govfix.service.GarbageVanTrackingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EscalationAndAiTests {

    @Autowired
    private EscalationService escalationService;

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private GarbageVanTrackingService garbageVanTrackingService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private EscalationRepository escalationRepository;

    @Autowired
    private AiRecommendationRepository aiRecommendationRepository;

    @Test
    @DisplayName("Verify simulated time advance triggers overdue escalations and prevents duplicates")
    public void testTimeAdvanceAndDuplicateEscalationPrevention() {
        // Create an open complaint with 24h deadline
        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle("Overdue streetlight in Bhosari");
        req.setDescription("Dark street near school");
        req.setCategory("Streetlights");
        req.setLocality("Bhosari");
        Complaint c = complaintService.createComplaint(req, "citizen_rahul", "CITIZEN");

        assertFalse(c.getIsEscalated());

        // Advance time by 48 hours to breach SLA
        List<Escalation> newEscalations = escalationService.advanceSimulatedTime(48);

        Complaint updated = complaintRepository.findById(c.getId()).orElseThrow();
        assertTrue(updated.getIsEscalated(), "Complaint should be flagged as escalated after time advance");

        // Run overdue check again to test duplicate escalation prevention
        List<Escalation> secondRun = escalationService.processOverdueComplaints(LocalDateTime.now(), "TEST_RUN");
        boolean hasDuplicate = secondRun.stream().anyMatch(e -> e.getComplaint().getId().equals(c.getId()));
        assertFalse(hasDuplicate, "Duplicate escalation must be prevented for already active escalations");
    }

    @Test
    @DisplayName("Verify AI recommendation requires human approval before updating complaint")
    public void testAiRecommendationWorkflow() {
        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle("Deep crater pothole on DP Road");
        req.setDescription("Two wheelers falling into crater near station");
        req.setCategory("Roads and potholes");
        req.setLocality("Akurdi");
        Complaint c = complaintService.createComplaint(req, "citizen_rahul", "CITIZEN");

        // 1. Generate AI recommendation
        AiSuggestionRequest aiReq = new AiSuggestionRequest(c.getTitle(), c.getDescription());
        aiReq.setComplaintId(c.getId());
        AiSuggestionResponse suggestion = aiRecommendationService.generateRecommendation(aiReq, "officer_sharma", "OFFICER");

        assertNotNull(suggestion.getRecommendationId());
        assertEquals("Roads and potholes", suggestion.getSuggestedCategory());
        assertEquals("Roads and Infrastructure", suggestion.getSuggestedDepartment());
        assertEquals("HIGH", suggestion.getSuggestedPriority());

        // Ensure recommendation is PENDING and complaint priority has NOT been changed automatically
        AiRecommendation savedRec = aiRecommendationRepository.findById(suggestion.getRecommendationId()).orElseThrow();
        assertEquals("PENDING", savedRec.getStatus());

        // 2. Human approval step
        AiDecisionRequest decision = new AiDecisionRequest();
        decision.setRecommendationId(suggestion.getRecommendationId());
        decision.setDecision("APPROVE");
        aiRecommendationService.processHumanDecision(decision, "supervisor_kulkarni", "SUPERVISOR");

        // Verify status is now APPROVED and complaint priority was updated
        AiRecommendation approvedRec = aiRecommendationRepository.findById(suggestion.getRecommendationId()).orElseThrow();
        assertEquals("APPROVED", approvedRec.getStatus());
        assertEquals("supervisor_kulkarni", approvedRec.getDecidedByUsername());

        Complaint updatedComplaint = complaintRepository.findById(c.getId()).orElseThrow();
        assertEquals("HIGH", updatedComplaint.getPriority());
    }

    @Test
    @DisplayName("Verify Garbage Collection Van GPS tracking and missed pickup reporting")
    public void testGarbageVanTrackingAndReporting() {
        // Test getting vans
        List<VanTrackingResponse> vans = garbageVanTrackingService.getAllVans();
        assertFalse(vans.isEmpty(), "Garbage collection vans should be seeded");

        // Test GPS simulation tick
        List<VanTrackingResponse> updatedVans = garbageVanTrackingService.simulateGpsStep();
        assertNotNull(updatedVans);

        // Test reporting missed pickup
        MissedCollectionReport report = new MissedCollectionReport();
        report.setVehicleNumber("MH-14-GH-1001");
        report.setLocality("Yamunanagar Sector 21");
        report.setCitizenName("Vikram Patil");
        report.setCitizenContact("9822334455");
        report.setNotes("Van did not arrive at 9:00 AM slot");

        Complaint missedGrievance = garbageVanTrackingService.reportMissedCollection(report);
        assertNotNull(missedGrievance.getId());
        assertTrue(missedGrievance.getTitle().contains("Missed Garbage Pickup"));
        assertEquals("Waste management", missedGrievance.getCategory());
        assertEquals("Waste Management", missedGrievance.getDepartment());
    }
}

