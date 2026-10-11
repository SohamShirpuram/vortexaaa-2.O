package com.govfix.repository;

import com.govfix.entity.GarbageVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarbageVanRepository extends JpaRepository<GarbageVan, Long> {
    Optional<GarbageVan> findByVehicleNumber(String vehicleNumber);
    List<GarbageVan> findByAssignedZone(String assignedZone);
}

