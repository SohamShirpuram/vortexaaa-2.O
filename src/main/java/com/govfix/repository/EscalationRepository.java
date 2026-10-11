package com.govfix.repository;

import com.govfix.entity.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationRepository extends JpaRepository<Escalation, Long> {
    List<Escalation> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
    List<Escalation> findAllByOrderByCreatedAtDesc();
    boolean existsByComplaintIdAndIsResolvedFalse(Long complaintId);
}

