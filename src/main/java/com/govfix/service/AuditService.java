package com.govfix.service;

import com.govfix.entity.AuditEvent;
import com.govfix.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Guarantees audit event persistence in an independent transaction boundary.
     * Even if the outer workflow transaction rolls back due to validation failure,
     * this audit entry will be safely committed and preserved.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditEvent logAuditEvent(String actorUsername, String actorRole, Long complaintId, String ticketId,
                                  String action, String previousState, String requestedState,
                                  String outcome, String reason) {
        try {
            AuditEvent event = new AuditEvent(
                    actorUsername != null ? actorUsername : "system",
                    actorRole != null ? actorRole : "SYSTEM",
                    complaintId,
                    ticketId,
                    action,
                    previousState,
                    requestedState,
                    outcome,
                    reason
            );
            AuditEvent saved = auditEventRepository.save(event);
            log.info("AUDIT [{}] Actor: {} ({}) | Ticket: {} | Action: {} | Outcome: {} | Reason: {}",
                    saved.getId(), actorUsername, actorRole, ticketId, action, outcome, reason);
            return saved;
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getAllAuditLogs() {
        return auditEventRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditLogsForTicket(String ticketId) {
        return auditEventRepository.findByTicketIdOrderByTimestampDesc(ticketId);
    }
}
