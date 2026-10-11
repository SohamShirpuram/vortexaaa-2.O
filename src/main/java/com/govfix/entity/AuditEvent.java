package com.govfix.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_username", nullable = false, length = 50)
    private String actorUsername;

    @Column(name = "actor_role", nullable = false, length = 30)
    private String actorRole;

    @Column(name = "complaint_id")
    private Long complaintId;

    @Column(name = "ticket_id", length = 30)
    private String ticketId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "previous_state", length = 50)
    private String previousState;

    @Column(name = "requested_state", length = 50)
    private String requestedState;

    @Column(nullable = false, length = 20)
    private String outcome; // SUCCESS, REJECTED

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditEvent() {}

    public AuditEvent(String actorUsername, String actorRole, Long complaintId, String ticketId, String action, String previousState, String requestedState, String outcome, String reason) {
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.complaintId = complaintId;
        this.ticketId = ticketId;
        this.action = action;
        this.previousState = previousState;
        this.requestedState = requestedState;
        this.outcome = outcome;
        this.reason = reason;
        this.clientIp = "127.0.0.1";
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPreviousState() { return previousState; }
    public void setPreviousState(String previousState) { this.previousState = previousState; }

    public String getRequestedState() { return requestedState; }
    public void setRequestedState(String requestedState) { this.requestedState = requestedState; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

