package com.govfix.repository;

import com.govfix.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    Optional<Complaint> findByTicketId(String ticketId);

    List<Complaint> findByStatus(String status);

    List<Complaint> findByAssignedOfficerId(Long officerId);

    List<Complaint> findByIsEscalatedTrue();

    List<Complaint> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.deadlineAt < :now AND c.resolvedAt IS NULL AND c.status NOT IN ('Resolved', 'Closed', 'Rejected')")
    long countOverdue(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.isEscalated = true")
    long countEscalated();

    @Query("SELECT c FROM Complaint c WHERE c.deadlineAt < :now AND c.resolvedAt IS NULL AND c.isEscalated = false AND c.status NOT IN ('Resolved', 'Closed', 'Rejected')")
    List<Complaint> findUnescalatedOverdue(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:locality IS NULL OR LOWER(c.locality) LIKE LOWER(CONCAT('%', :locality, '%'))) " +
           "ORDER BY c.createdAt DESC")
    List<Complaint> searchComplaints(@Param("status") String status,
                                    @Param("category") String category,
                                    @Param("priority") String priority,
                                    @Param("locality") String locality);
}
