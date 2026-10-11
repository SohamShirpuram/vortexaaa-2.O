package com.govfix.service;

import com.govfix.dto.ComplaintCreateRequest;
import com.govfix.dto.MissedCollectionReport;
import com.govfix.dto.VanTrackingResponse;
import com.govfix.entity.Complaint;
import com.govfix.entity.GarbageVan;
import com.govfix.entity.GarbageVanStop;
import com.govfix.repository.GarbageVanRepository;
import com.govfix.repository.GarbageVanStopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GarbageVanTrackingService {

    private static final Logger log = LoggerFactory.getLogger(GarbageVanTrackingService.class);

    private final GarbageVanRepository garbageVanRepository;
    private final GarbageVanStopRepository garbageVanStopRepository;
    private final ComplaintService complaintService;

    // Simulation step index per van
    private int simulationTickCount = 0;

    public GarbageVanTrackingService(GarbageVanRepository garbageVanRepository,
                                    GarbageVanStopRepository garbageVanStopRepository,
                                    ComplaintService complaintService) {
        this.garbageVanRepository = garbageVanRepository;
        this.garbageVanStopRepository = garbageVanStopRepository;
        this.complaintService = complaintService;
    }

    @Transactional(readOnly = true)
    public List<VanTrackingResponse> getAllVans() {
        List<GarbageVan> vans = garbageVanRepository.findAll();
        List<VanTrackingResponse> responses = new ArrayList<>();
        for (GarbageVan van : vans) {
            responses.add(mapToDto(van));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public Optional<VanTrackingResponse> getVanById(Long id) {
        return garbageVanRepository.findById(id).map(this::mapToDto);
    }

    /**
     * Simulates GPS movement for all active vans along their Pimpri Chinchwad routes.
     * Advances coordinates smoothly, updates headings, and recalculates ETAs.
     */
    @Transactional
    public List<VanTrackingResponse> simulateGpsStep() {
        simulationTickCount++;
        List<GarbageVan> vans = garbageVanRepository.findAll();

        for (GarbageVan van : vans) {
            List<GarbageVanStop> stops = van.getStops();
            if (stops.isEmpty()) continue;

            // Find current uncompleted stop
            Optional<GarbageVanStop> nextStopOpt = stops.stream()
                    .filter(s -> !Boolean.TRUE.equals(s.getIsCompleted()))
                    .findFirst();

            if (nextStopOpt.isPresent()) {
                GarbageVanStop target = nextStopOpt.get();
                double dLat = target.getLatitude() - van.getCurrentLatitude();
                double dLng = target.getLongitude() - van.getCurrentLongitude();
                double distance = Math.sqrt(dLat * dLat + dLng * dLng);

                if (distance < 0.0008) {
                    // Reached stop! Mark completed
                    target.setIsCompleted(true);
                    target.setEstimatedArrivalMinutes(0);
                    garbageVanStopRepository.save(target);

                    // Move to next stop
                    stops.stream()
                            .filter(s -> !Boolean.TRUE.equals(s.getIsCompleted()))
                            .findFirst()
                            .ifPresent(next -> next.setEstimatedArrivalMinutes(Math.max(3, next.getEstimatedArrivalMinutes() - 2)));
                } else {
                    // Interpolate step towards target stop (simulated vehicle speed ~20 km/h)
                    double step = 0.00035;
                    van.setCurrentLatitude(van.getCurrentLatitude() + (dLat / distance) * step);
                    van.setCurrentLongitude(van.getCurrentLongitude() + (dLng / distance) * step);

                    // Calculate angle for vehicle heading icon
                    double angle = Math.toDegrees(Math.atan2(dLng, dLat));
                    if (angle < 0) angle += 360;
                    van.setHeading(angle);

                    // Decrement ETA slightly
                    int currentEta = target.getEstimatedArrivalMinutes();
                    if (currentEta > 1 && simulationTickCount % 2 == 0) {
                        target.setEstimatedArrivalMinutes(currentEta - 1);
                        garbageVanStopRepository.save(target);
                    }
                }
            } else {
                // All stops completed - loop route for infinite smooth demo
                for (int i = 0; i < stops.size(); i++) {
                    GarbageVanStop s = stops.get(i);
                    s.setIsCompleted(i == 0);
                    s.setEstimatedArrivalMinutes((i + 1) * 7);
                    garbageVanStopRepository.save(s);
                }
                if (!stops.isEmpty()) {
                    van.setCurrentLatitude(stops.get(0).getLatitude());
                    van.setCurrentLongitude(stops.get(0).getLongitude());
                }
            }

            van.setLastUpdatedAt(LocalDateTime.now());
            garbageVanRepository.save(van);
        }

        return getAllVans();
    }

    /**
     * Converts a citizen missed-pickup report into a trackable GovFix grievance
     */
    @Transactional
    public Complaint reportMissedCollection(MissedCollectionReport report) {
        ComplaintCreateRequest createReq = new ComplaintCreateRequest();
        createReq.setTitle("Missed Garbage Pickup - " + report.getLocality() +
                (report.getVehicleNumber() != null ? " (" + report.getVehicleNumber() + ")" : ""));
        createReq.setDescription("Citizen reported missed door-to-door waste collection. " +
                (report.getNotes() != null ? report.getNotes() : "Vehicle did not arrive during scheduled route slot."));
        createReq.setCategory("Waste management");
        createReq.setLocality(report.getLocality());
        createReq.setLandmark(report.getLandmark() != null ? report.getLandmark() : "Near residential gate");
        createReq.setPriority("HIGH");
        createReq.setCitizenName(report.getCitizenName());
        createReq.setCitizenContact(report.getCitizenContact());

        return complaintService.createComplaint(createReq, "citizen_" + report.getCitizenName().replaceAll("\\s+", "").toLowerCase(), "CITIZEN");
    }

    private VanTrackingResponse mapToDto(GarbageVan van) {
        VanTrackingResponse dto = new VanTrackingResponse();
        dto.setId(van.getId());
        dto.setVehicleNumber(van.getVehicleNumber());
        dto.setDriverName(van.getDriverName());
        dto.setContactPhone(van.getContactPhone());
        dto.setAssignedZone(van.getAssignedZone());
        dto.setCurrentLatitude(van.getCurrentLatitude());
        dto.setCurrentLongitude(van.getCurrentLongitude());
        dto.setHeading(van.getHeading());
        dto.setSpeedKmph(van.getSpeedKmph());
        dto.setStatus(van.getStatus());
        dto.setLastUpdatedAt(van.getLastUpdatedAt());
        dto.setSimulated(true);

        List<VanTrackingResponse.StopDto> stopDtos = new ArrayList<>();
        int nextEta = 0;
        String nextLocality = "Route Completed";
        boolean foundNext = false;

        for (GarbageVanStop stop : van.getStops()) {
            VanTrackingResponse.StopDto sd = new VanTrackingResponse.StopDto();
            sd.setId(stop.getId());
            sd.setLocalityName(stop.getLocalityName());
            sd.setStopOrder(stop.getStopOrder());
            sd.setLatitude(stop.getLatitude());
            sd.setLongitude(stop.getLongitude());
            sd.setScheduledArrivalTime(stop.getScheduledArrivalTime());
            sd.setIsCompleted(stop.getIsCompleted());
            sd.setEstimatedArrivalMinutes(stop.getEstimatedArrivalMinutes());
            stopDtos.add(sd);

            if (!Boolean.TRUE.equals(stop.getIsCompleted()) && !foundNext) {
                nextEta = stop.getEstimatedArrivalMinutes() != null ? stop.getEstimatedArrivalMinutes() : 10;
                nextLocality = stop.getLocalityName();
                foundNext = true;
            }
        }

        dto.setStops(stopDtos);
        dto.setNextStopEtaMinutes(nextEta);
        dto.setNextStopLocality(nextLocality);
        return dto;
    }
}

