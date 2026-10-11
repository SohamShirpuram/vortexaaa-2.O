package com.govfix.service;

import com.govfix.entity.*;
import com.govfix.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataInitializerService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializerService.class);

    private final UserRepository userRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStateRepository workflowStateRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final ComplaintRepository complaintRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final EscalationRepository escalationRepository;
    private final AuditEventRepository auditEventRepository;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final GarbageVanRepository garbageVanRepository;
    private final GarbageVanStopRepository garbageVanStopRepository;

    public DataInitializerService(UserRepository userRepository,
                                  WorkflowDefinitionRepository workflowDefinitionRepository,
                                  WorkflowStateRepository workflowStateRepository,
                                  WorkflowTransitionRepository workflowTransitionRepository,
                                  ComplaintRepository complaintRepository,
                                  EvidenceRecordRepository evidenceRecordRepository,
                                  EscalationRepository escalationRepository,
                                  AuditEventRepository auditEventRepository,
                                  AiRecommendationRepository aiRecommendationRepository,
                                  GarbageVanRepository garbageVanRepository,
                                  GarbageVanStopRepository garbageVanStopRepository) {
        this.userRepository = userRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.workflowStateRepository = workflowStateRepository;
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.complaintRepository = complaintRepository;
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.escalationRepository = escalationRepository;
        this.auditEventRepository = auditEventRepository;
        this.aiRecommendationRepository = aiRecommendationRepository;
        this.garbageVanRepository = garbageVanRepository;
        this.garbageVanStopRepository = garbageVanStopRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0 || workflowDefinitionRepository.count() == 0) {
            log.info("Database is empty. Seeding GovFix municipal demo datasets for PCMC...");
            seedAllData();
            log.info("GovFix demo data initialized successfully!");
        } else {
            log.info("GovFix database already contains data ({} users, {} complaints). Skipping initial seed.",
                    userRepository.count(), complaintRepository.count());
        }
    }

    @Transactional
    public void resetDemoData() {
        log.warn("RESET DEMO DATA requested. Purging existing transactional data and reseeding fresh demo state...");
        aiRecommendationRepository.deleteAll();
        evidenceRecordRepository.deleteAll();
        escalationRepository.deleteAll();
        auditEventRepository.deleteAll();
        garbageVanStopRepository.deleteAll();
        garbageVanRepository.deleteAll();
        complaintRepository.deleteAll();
        workflowTransitionRepository.deleteAll();
        workflowStateRepository.deleteAll();
        workflowDefinitionRepository.deleteAll();
        userRepository.deleteAll();

        seedAllData();
        log.info("Demo reset complete!");
    }

    private void seedAllData() {
        // 1. Users
        User citizen = userRepository.save(new User("citizen_rahul", "{noop}password123", "Rahul Deshmukh", "rahul.deshmukh@example.com", "9822012345", Role.CITIZEN, "Citizen"));
        User officerRoads = userRepository.save(new User("officer_sharma", "{noop}password123", "Amit Sharma (JE Road Dept)", "amit.sharma@pcmc-demo.gov.in", "9822023456", Role.OFFICER, "Roads and Infrastructure"));
        User officerWaste = userRepository.save(new User("officer_patil", "{noop}password123", "Sunita Patil (Health Inspector)", "sunita.patil@pcmc-demo.gov.in", "9822034567", Role.OFFICER, "Waste Management"));
        User supervisor = userRepository.save(new User("supervisor_kulkarni", "{noop}password123", "Dr. Anand Kulkarni (Executive Engineer)", "anand.kulkarni@pcmc-demo.gov.in", "9822045678", Role.SUPERVISOR, "Public Works & Sanitation"));
        User admin = userRepository.save(new User("admin_govfix", "{noop}password123", "System Administrator", "admin@pcmc-demo.gov.in", "9822056789", Role.ADMIN, "Municipal Administration"));
        User auditor = userRepository.save(new User("auditor_joshi", "{noop}password123", "Vikas Joshi (Vigilance Officer)", "vikas.joshi@pcmc-demo.gov.in", "9822067890", Role.AUDITOR, "Chief Auditor Office"));

        // 2. Default Workflow: Pothole & Civic Road Repair
        WorkflowDefinition potholeWf = new WorkflowDefinition(
                "pothole_workflow",
                "Roads & Potholes Resolution Workflow",
                "Standard 8-stage civic lifecycle with mandatory before/after photo inspection",
                "Submitted",
                "Roads and potholes"
        );
        potholeWf = workflowDefinitionRepository.save(potholeWf);

        // States for Pothole Workflow
        workflowStateRepository.save(new WorkflowState(potholeWf, "Submitted", "Submitted", 1, 24, false, false, "#64748b"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Under Review", "Under Review", 2, 24, false, false, "#3b82f6"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Assigned", "Assigned", 3, 24, false, false, "#0284c7"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Inspection Required", "Inspection Required", 4, 24, false, false, "#eab308"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "In Progress", "In Progress", 5, 48, false, false, "#f97316"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Resolution Submitted", "Resolution Submitted", 6, 24, false, true, "#8b5cf6"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Resolved", "Resolved", 7, 48, false, false, "#10b981"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Closed", "Closed", 8, 0, true, false, "#0f766e"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Information Required", "Information Required", 9, 48, false, false, "#ec4899"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Rejected", "Rejected", 10, 0, true, false, "#ef4444"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Reopened", "Reopened", 11, 24, false, false, "#dc2626"));
        workflowStateRepository.save(new WorkflowState(potholeWf, "Escalated", "Escalated", 12, 12, false, false, "#b91c1c"));

        // Transitions for Pothole Workflow
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Submitted", "Under Review", "Initiate Review", "OFFICER,SUPERVISOR,ADMIN", false, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Under Review", "Assigned", "Assign Field Officer", "SUPERVISOR,ADMIN", true, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Under Review", "Rejected", "Reject Invalid Complaint", "SUPERVISOR,ADMIN", true, false, true));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Assigned", "Inspection Required", "Request Site Inspection", "OFFICER,SUPERVISOR", false, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Inspection Required", "In Progress", "Record Initial Inspection & Begin Work", "OFFICER", true, true, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "In Progress", "Resolution Submitted", "Submit Work Completion Evidence", "OFFICER", true, true, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Resolution Submitted", "Resolved", "Approve Quality & Mark Resolved", "SUPERVISOR,ADMIN", true, false, true));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Resolution Submitted", "In Progress", "Reject Resolution (Rework Needed)", "SUPERVISOR,ADMIN", true, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Resolved", "Closed", "Confirm Citizen Satisfaction", "CITIZEN,ADMIN", false, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Resolved", "Reopened", "Citizen Reopen Request", "CITIZEN,ADMIN", true, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Reopened", "In Progress", "Acknowledge Reopened Case", "SUPERVISOR,ADMIN", true, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Submitted", "Escalated", "Escalate Stalled Ticket", "SUPERVISOR,ADMIN", true, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Assigned", "Escalated", "Escalate Stalled Ticket", "SUPERVISOR,ADMIN", true, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "In Progress", "Escalated", "Escalate Stalled Ticket", "SUPERVISOR,ADMIN", true, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(potholeWf, "Escalated", "In Progress", "De-escalate & Resume Work", "SUPERVISOR,ADMIN", true, false, false));

        // 3. Second Workflow: Water Supply & Contamination (Demonstrating multi-workflow configurability!)
        WorkflowDefinition waterWf = new WorkflowDefinition(
                "water_supply_workflow",
                "Water Supply & Contamination Workflow",
                "Fast-tracked civic pipeline for pipeline leakage and drinking water quality",
                "Submitted",
                "Water supply"
        );
        waterWf = workflowDefinitionRepository.save(waterWf);

        workflowStateRepository.save(new WorkflowState(waterWf, "Submitted", "Submitted", 1, 12, false, false, "#64748b"));
        workflowStateRepository.save(new WorkflowState(waterWf, "Assigned", "Assigned", 2, 12, false, false, "#0284c7"));
        workflowStateRepository.save(new WorkflowState(waterWf, "In Progress", "Repair In Progress", 3, 24, false, false, "#f97316"));
        workflowStateRepository.save(new WorkflowState(waterWf, "Resolved", "Resolved", 4, 12, false, false, "#10b981"));
        workflowStateRepository.save(new WorkflowState(waterWf, "Closed", "Closed", 5, 0, true, false, "#0f766e"));

        workflowTransitionRepository.save(new WorkflowTransition(waterWf, "Submitted", "Assigned", "Quick Dispatch Team", "SUPERVISOR,OFFICER,ADMIN", false, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(waterWf, "Assigned", "In Progress", "Start Pipeline Repair", "OFFICER", true, false, false));
        workflowTransitionRepository.save(new WorkflowTransition(waterWf, "In Progress", "Resolved", "Pressure Restored & Fixed", "OFFICER,SUPERVISOR", true, true, false));
        workflowTransitionRepository.save(new WorkflowTransition(waterWf, "Resolved", "Closed", "Citizen Confirmed Flow", "CITIZEN,ADMIN", false, false, false));

        // 4. Seed 10 Fictional Complaints across Pimpri Chinchwad localities
        Complaint c1 = createSeedComplaint("GV-1001", "Severe deep crater pothole near Akurdi Railway Station",
                "Two-wheeler riders skidding due to a 2-foot wide pothole right outside station exit gate.",
                "Roads and potholes", "Roads and Infrastructure", "Akurdi", "Ward 14 (Nigdi Pradhikaran)", "Opposite Railway Gate 2",
                "HIGH", "Inspection Required", "Rahul Deshmukh", "9822012345", officerRoads,
                "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600",
                LocalDateTime.now().plusHours(18), false, LocalDateTime.now().minusHours(24));

        Complaint c2 = createSeedComplaint("GV-1002", "Broken water distribution pipeline leaking clean water",
                "Continuous leakage on main DP road causing water wastage and low pressure in nearby housing societies.",
                "Water supply", "Water Supply & Quality", "Wakad", "Ward 25 (Wakad - Hinjawadi)", "Near Dutta Mandir Chowk",
                "CRITICAL", "In Progress", "Pooja Jadhav", "9822098765", officerRoads,
                "https://images.unsplash.com/photo-1541888946425-d0fbb18f15f7?w=600",
                LocalDateTime.now().minusHours(4), true, LocalDateTime.now().minusHours(52));

        Complaint c3 = createSeedComplaint("GV-1003", "Overflowing community garbage dumpster near vegetable market",
                "Waste not collected for 3 days, strays spreading garbage on the street.",
                "Waste management", "Waste Management", "Bhosari", "Ward 8 (Bhosari Gaonthan)", "Weekly Bazaar Ground",
                "HIGH", "Assigned", "Suresh Shinde", "9822087654", officerWaste,
                "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=600",
                LocalDateTime.now().plusHours(12), false, LocalDateTime.now().minusHours(16));

        Complaint c4 = createSeedComplaint("GV-1004", "Non-functional streetlights on Kunal Icon Road",
                "Entire stretch of 8 streetlights dark for past two weeks posing safety hazard for pedestrians.",
                "Streetlights", "Electrical Department", "Pimple Saudagar", "Ward 31 (Pimple Saudagar)", "Opposite Roseland Residency",
                "MEDIUM", "Submitted", "Anjali Kulkarni", "9822076543", null,
                null, LocalDateTime.now().plusHours(22), false, LocalDateTime.now().minusHours(2));

        Complaint c5 = createSeedComplaint("GV-1005", "Open manhole cover on footpath near bus stop",
                "Dangerous missing cement cover on stormwater drain, children waiting for school bus.",
                "Drainage and sanitation", "Drainage Department", "Chinchwad", "Ward 19 (Chinchwad Station)", "Near Elpro City Square Mall",
                "CRITICAL", "Resolution Submitted", "Vipin More", "9822065432", officerRoads,
                "https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=600",
                LocalDateTime.now().plusHours(6), false, LocalDateTime.now().minusHours(36));

        Complaint c6 = createSeedComplaint("GV-1006", "Fallen tree branch blocking walkway in Durga Tekdi Garden",
                "Heavy rain branch fell over children playground pathway.",
                "Public gardens", "Garden Department", "Nigdi", "Ward 12 (Pradhikaran)", "Durga Tekdi Sector 26",
                "LOW", "Resolved", "Kavita Naik", "9822054321", officerWaste,
                "https://images.unsplash.com/photo-1448375240586-882707db888b?w=600",
                LocalDateTime.now().minusHours(12), false, LocalDateTime.now().minusHours(72));

        Complaint c7 = createSeedComplaint("GV-1007", "Contaminated muddy tap water in Sector 21",
                "Yellowish water with unpleasant odor received during morning supply slot.",
                "Water supply", "Water Supply & Quality", "Nigdi", "Ward 11 (Yamunanagar)", "Sector 21 Near Water Tank",
                "CRITICAL", "Escalated", "Mahesh Bansal", "9822043210", officerRoads,
                null, LocalDateTime.now().minusHours(18), true, LocalDateTime.now().minusHours(60));

        Complaint c8 = createSeedComplaint("GV-1008", "Multiple potholes after monsoon showers on BRTS corridor",
                "Asphalt washed away causing vehicular bottleneck during peak morning hours.",
                "Roads and potholes", "Roads and Infrastructure", "Ravet", "Ward 16 (Ravet-Kiwale)", "Near D.Y. Patil College Campus",
                "HIGH", "Under Review", "Sachin Ghorpade", "9822032109", null,
                "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600",
                LocalDateTime.now().plusHours(14), false, LocalDateTime.now().minusHours(10));

        Complaint c9 = createSeedComplaint("GV-1009", "Missed door-to-door garbage collection vehicle",
                "Waste collection van did not arrive at our society street today.",
                "Waste management", "Waste Management", "Sangvi", "Ward 33 (Old Sangvi)", "Ganga Nagar Lane 4",
                "MEDIUM", "Submitted", "Meena Jagtap", "9822021098", null,
                null, LocalDateTime.now().plusHours(20), false, LocalDateTime.now().minusHours(4));

        Complaint c10 = createSeedComplaint("GV-1010", "Clogged drainage causing waterlogging on service road",
                "Sewage overflow onto pedestrian walkway near highway underpass.",
                "Drainage and sanitation", "Drainage Department", "Kasarwadi", "Ward 22 (Kasarwadi-Phugewadi)", "Highway Underpass Pillar 12",
                "HIGH", "Closed", "Rajesh Gaikwad", "9822010987", officerRoads,
                null, LocalDateTime.now().minusHours(48), false, LocalDateTime.now().minusHours(96));

        // 5. Evidence Records
        evidenceRecordRepository.save(new EvidenceRecord(c5, officerRoads, "BEFORE_PHOTO", "manhole_before_repair.jpg", "https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=600", 245000L, "image/jpeg", "Site inspection confirmed missing pre-cast cover. Barricaded immediately."));
        evidenceRecordRepository.save(new EvidenceRecord(c5, officerRoads, "AFTER_PHOTO", "manhole_fixed_reinforced.jpg", "https://images.unsplash.com/photo-1584467735871-8e85353a8413?w=600", 312000L, "image/jpeg", "New heavy-duty SFRC cover installed and leveled with asphalt footpath."));
        evidenceRecordRepository.save(new EvidenceRecord(c1, officerRoads, "INSPECTION_REPORT", "site_inspection_measurements.pdf", "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600", 120400L, "application/pdf", "Site measured: 1.8m x 1.2m depth 12cm. Cold mix asphalt batch requisitioned."));

        // 6. Escalations
        escalationRepository.save(new Escalation(c2, 1, "Standard 48-hour deadline breached without resolution submission. [Simulated Demo SLA]", "SYSTEM_DEADLINE", "SUPERVISOR"));
        escalationRepository.save(new Escalation(c7, 2, "Contaminated drinking water unaddressed past critical 24-hour SLA. Escalated directly to Executive Engineer.", "SUPERVISOR_MANUAL", "SUPERVISOR"));

        // 7. Audit Events
        auditEventRepository.save(new AuditEvent("citizen_rahul", "CITIZEN", c1.getId(), c1.getTicketId(), "SUBMIT_COMPLAINT", null, "Submitted", "SUCCESS", "New pothole grievance reported via PCMC Citizen Portal"));
        auditEventRepository.save(new AuditEvent("officer_sharma", "OFFICER", c1.getId(), c1.getTicketId(), "TRANSITION", "Submitted", "Under Review", "SUCCESS", "Officer took complaint for technical validation"));
        auditEventRepository.save(new AuditEvent("supervisor_kulkarni", "SUPERVISOR", c1.getId(), c1.getTicketId(), "ASSIGN_OFFICER", "Under Review", "Assigned", "SUCCESS", "Assigned to field engineer Amit Sharma"));
        auditEventRepository.save(new AuditEvent("officer_sharma", "OFFICER", c1.getId(), c1.getTicketId(), "TRANSITION", "Assigned", "Inspection Required", "SUCCESS", "Site inspection initiated"));
        auditEventRepository.save(new AuditEvent("officer_sharma", "OFFICER", c1.getId(), c1.getTicketId(), "TRANSITION_ATTEMPT", "Inspection Required", "Resolved", "REJECTED", "Illegal transition: Cannot skip In Progress and missing completion evidence photos"));
        auditEventRepository.save(new AuditEvent("system_escalation", "SYSTEM", c2.getId(), c2.getTicketId(), "DEADLINE_ESCALATION", "In Progress", "In Progress", "SUCCESS", "Simulated 48-hour SLA expired. Automated escalation triggered to Supervisor."));

        // 8. AI Recommendations
        aiRecommendationRepository.save(new AiRecommendation(c1, "Roads and potholes", "Roads and Infrastructure", "HIGH", 0.94, "Identified road hazard keyword 'deep crater pothole' and station vicinity risk."));
        aiRecommendationRepository.save(new AiRecommendation(c4, "Streetlights", "Electrical Department", "MEDIUM", 0.91, "Identified public lighting failure keywords 'non-functional streetlights'."));
        aiRecommendationRepository.save(new AiRecommendation(c9, "Waste management", "Waste Management", "MEDIUM", 0.96, "Identified municipal sanitation keywords 'door-to-door garbage collection vehicle'."));

        // 9. Garbage Collection Vans & Waypoints (Featured Tracking Module in PCMC)
        GarbageVan van1 = garbageVanRepository.save(new GarbageVan("MH-14-GH-1001", "Santosh Pawar", "9822112233", "Zone A: Nigdi - Pradhikaran", 18.6540, 73.7715, 65.0, 19.5, "ON_DUTY"));
        GarbageVan van2 = garbageVanRepository.save(new GarbageVan("MH-14-GH-1002", "Ganesh Kadam", "9822114455", "Zone B: Wakad - Hinjawadi", 18.5992, 73.7634, 110.0, 16.0, "ON_DUTY"));
        GarbageVan van3 = garbageVanRepository.save(new GarbageVan("MH-14-GH-1003", "Ramesh Gaikwad", "9822116677", "Zone C: Bhosari - MIDC", 18.6278, 73.8441, 45.0, 22.0, "ON_DUTY"));

        garbageVanStopRepository.save(new GarbageVanStop(van1, "Sector 24 Pradhikaran", 1, 18.6480, 73.7650, "07:30 AM", true, 0));
        garbageVanStopRepository.save(new GarbageVanStop(van1, "Akurdi Railway Station Chowk", 2, 18.6515, 73.7680, "08:00 AM", true, 0));
        garbageVanStopRepository.save(new GarbageVanStop(van1, "Bhakti Shakti Chowk, Nigdi", 3, 18.6540, 73.7715, "08:30 AM", false, 4));
        garbageVanStopRepository.save(new GarbageVanStop(van1, "Yamunanagar Sector 21", 4, 18.6600, 73.7760, "09:00 AM", false, 16));
        garbageVanStopRepository.save(new GarbageVanStop(van1, "Rupeenagar Main Chowk", 5, 18.6680, 73.7820, "09:30 AM", false, 28));

        garbageVanStopRepository.save(new GarbageVanStop(van2, "Dutta Mandir Chowk, Wakad", 1, 18.5970, 73.7600, "07:45 AM", true, 0));
        garbageVanStopRepository.save(new GarbageVanStop(van2, "Kaspate Vasti, Wakad", 2, 18.5992, 73.7634, "08:15 AM", false, 5));
        garbageVanStopRepository.save(new GarbageVanStop(van2, "Bhumkar Chowk", 3, 18.6025, 73.7550, "08:45 AM", false, 18));
        garbageVanStopRepository.save(new GarbageVanStop(van2, "Hinjawadi Phase 1 Shivaji Chowk", 4, 18.5912, 73.7389, "09:15 AM", false, 32));

        garbageVanStopRepository.save(new GarbageVanStop(van3, "Bhosari Dighi Road", 1, 18.6250, 73.8400, "08:00 AM", true, 0));
        garbageVanStopRepository.save(new GarbageVanStop(van3, "Bhosari Gaonthan Bazaar", 2, 18.6278, 73.8441, "08:30 AM", false, 3));
        garbageVanStopRepository.save(new GarbageVanStop(van3, "MIDC Bhosari Block J", 3, 18.6320, 73.8490, "09:00 AM", false, 15));
        garbageVanStopRepository.save(new GarbageVanStop(van3, "Indrayani Nagar Sector 1", 4, 18.6380, 73.8550, "09:30 AM", false, 25));
    }

    private Complaint createSeedComplaint(String ticketId, String title, String desc, String cat, String dept,
                                          String locality, String ward, String landmark, String priority,
                                          String status, String citizenName, String contact, User officer,
                                          String photoUrl, LocalDateTime deadline, boolean isEscalated, LocalDateTime createdAt) {
        Complaint c = new Complaint();
        c.setTicketId(ticketId);
        c.setTitle(title);
        c.setDescription(desc);
        c.setCategory(cat);
        c.setDepartment(dept);
        c.setLocality(locality);
        c.setWardOrZone(ward);
        c.setLandmark(landmark);
        c.setPriority(priority);
        c.setStatus(status);
        c.setCitizenName(citizenName);
        c.setCitizenContact(contact);
        c.setAssignedOfficer(officer);
        c.setPhotoUrl(photoUrl);
        c.setDeadlineAt(deadline);
        c.setIsEscalated(isEscalated);
        c.setCreatedAt(createdAt);
        c.setUpdatedAt(createdAt);
        if ("Resolved".equalsIgnoreCase(status)) c.setResolvedAt(createdAt.plusHours(20));
        if ("Closed".equalsIgnoreCase(status)) c.setClosedAt(createdAt.plusHours(30));
        return complaintRepository.save(c);
    }
}
