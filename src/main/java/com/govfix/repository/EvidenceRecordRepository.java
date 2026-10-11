package com.govfix.repository;

import com.govfix.entity.EvidenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRecordRepository extends JpaRepository<EvidenceRecord, Long> {
    List<EvidenceRecord> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
    long countByComplaintIdAndEvidenceType(Long complaintId, String evidenceType);
    long countByComplaintId(Long complaintId);
}
