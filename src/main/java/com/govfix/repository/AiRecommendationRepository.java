package com.govfix.repository;

import com.govfix.entity.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {
    List<AiRecommendation> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
    Optional<AiRecommendation> findFirstByComplaintIdOrderByCreatedAtDesc(Long complaintId);
    List<AiRecommendation> findByStatusOrderByCreatedAtDesc(String status);
}
