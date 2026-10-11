package com.govfix.repository;

import com.govfix.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findAllByOrderByTimestampDesc();
    List<AuditEvent> findByComplaintIdOrderByTimestampDesc(Long complaintId);
    List<AuditEvent> findByTicketIdOrderByTimestampDesc(String ticketId);
    List<AuditEvent> findByOutcomeOrderByTimestampDesc(String outcome);
}

