package com.govfix.service;

import com.govfix.entity.Complaint;
import com.govfix.entity.Escalation;
import com.govfix.repository.ComplaintRepository;
import com.govfix.repository.EscalationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationService.class);

    private final ComplaintRepository complaintRepository;
    private final EscalationRepository escalationRepository;
    private final AuditService auditService;

    public EscalationService(ComplaintRepository complaintRepository,
                             EscalationRepository escalationRepository,
                             AuditService auditService) {
        this.complaintRepository = complaintRepository;
        this.escalationRepository = escalationRepository;
        this.auditService = auditService;
    }

    /**
     * Periodic scanner for overdue complaints (every 60 seconds)
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndEscalateOverdueComplaints() {
        processOverdueComplaints(LocalDateTime.now(), "SYSTEM_SCHEDULED");
    }

    /**
     * Advances simulated time by specified hours for judging/demo purposes.
     * Shifts deadlines backwards or evaluates overdue tickets against advanced clock.
     */
    @Transactional
    public List<Escalation> advanceSimulatedTime(int hours) {
        log.info("SIMULATION: Advancing simulated clock by {} hours", hours);
        LocalDateTime advancedNow = LocalDateTime.now().plusHours(hours);

        // Adjust open complaints' deadline backwards to trigger realistic deadline expiry
        List<Complaint> openComplaints = complaintRepository.findAll().stream()
                .filter(c -> c.getDeadlineAt() != null && c.getResolvedAt() == null
                        && !"Closed".equalsIgnoreCase(c.getStatus())
                        && !"Resolved".equalsIgnoreCase(c.getStatus())
                        && !"Rejected".equalsIgnoreCase(c.getStatus()))
                .toList();

        for (Complaint c : openComplaints) {
            c.setDeadlineAt(c.getDeadlineAt().minusHours(hours));
            complaintRepository.save(c);
        }

        List<Escalation> triggered = processOverdueComplaints(LocalDateTime.now(), "DEMO_SIMULATED_TIME_ADVANCE (+" + hours + "h)");

        auditService.logAuditEvent("demo_admin", "ADMIN", null, null,
                "SIMULATE_TIME_ADVANCE", null, null, "SUCCESS",
                "Advanced time by " + hours + " hours. Created " + triggered.size() + " new escalation(s).");

        return triggered;
    }

    @Transactional
    public List<Escalation> processOverdueComplaints(LocalDateTime referenceTime, String triggerSource) {
        List<Complaint> overdueUnescalated = complaintRepository.findUnescalatedOverdue(referenceTime);
        List<Escalation> newEscalations = new ArrayList<>();

        for (Complaint c : overdueUnescalated) {
            // Duplicate-escalation prevention
            if (escalationRepository.existsByComplaintIdAndIsResolvedFalse(c.getId())) {
                continue;
            }

            Escalation esc = new Escalation(
                    c,
                    1,
                    String.format("Stage deadline exceeded for status '%s'. SLA expired at %s [Simulated Demo SLA]",
                            c.getStatus(), c.getDeadlineAt()),
                    triggerSource,
                    "SUPERVISOR"
            );
            Escalation saved = escalationRepository.save(esc);
            newEscalations.add(saved);

            // Mark complaint as escalated
            c.setIsEscalated(true);
            complaintRepository.save(c);

            auditService.logAuditEvent("system_escalation", "SYSTEM", c.getId(), c.getTicketId(),
                    "ESCALATION_TRIGGERED", c.getStatus(), c.getStatus(), "SUCCESS",
                    "Overdue breach: Complaint escalated to Supervisor. " + esc.getReason());
        }

        return newEscalations;
    }

    /**
     * Manual escalation by supervisor or admin
     */
    @Transactional
    public Escalation manualEscalate(Long complaintId, String reason, String actorUsername, String actorRole) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found: " + complaintId));

        if (escalationRepository.existsByComplaintIdAndIsResolvedFalse(complaint.getId())) {
            throw new IllegalStateException("An active escalation already exists for ticket: " + complaint.getTicketId());
        }

        Escalation esc = new Escalation(
                complaint,
                2,
                reason,
                "SUPERVISOR_MANUAL",
                "SUPERVISOR"
        );
        Escalation saved = escalationRepository.save(esc);

        complaint.setIsEscalated(true);
        complaintRepository.save(complaint);

        auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                "MANUAL_ESCALATION", complaint.getStatus(), complaint.getStatus(), "SUCCESS",
                "Supervisor escalated ticket: " + reason);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Escalation> getAllEscalations() {
        return escalationRepository.findAllByOrderByCreatedAtDesc();
    }
}
