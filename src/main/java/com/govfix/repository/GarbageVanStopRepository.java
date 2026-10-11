package com.govfix.repository;

import com.govfix.entity.GarbageVanStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarbageVanStopRepository extends JpaRepository<GarbageVanStop, Long> {
    List<GarbageVanStop> findByVanIdOrderByStopOrderAsc(Long vanId);
}

