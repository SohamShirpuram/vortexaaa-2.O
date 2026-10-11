package com.govfix.controller;

import com.govfix.dto.ApiResponse;
import com.govfix.dto.MissedCollectionReport;
import com.govfix.dto.VanTrackingResponse;
import com.govfix.entity.Complaint;
import com.govfix.service.GarbageVanTrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/garbage-tracking")
public class GarbageVanController {

    private final GarbageVanTrackingService trackingService;

    public GarbageVanController(GarbageVanTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/vans")
    public ResponseEntity<ApiResponse<List<VanTrackingResponse>>> getVans() {
        List<VanTrackingResponse> list = trackingService.getAllVans();
        return ResponseEntity.ok(ApiResponse.ok("Live garbage collection vehicles retrieved", list));
    }

    @GetMapping("/vans/{id}")
    public ResponseEntity<ApiResponse<VanTrackingResponse>> getVanById(@PathVariable Long id) {
        return trackingService.getVanById(id)
                .map(van -> ResponseEntity.ok(ApiResponse.ok("Vehicle tracking details", van)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/simulate-tick")
    public ResponseEntity<ApiResponse<List<VanTrackingResponse>>> simulateTick() {
        List<VanTrackingResponse> updated = trackingService.simulateGpsStep();
        return ResponseEntity.ok(ApiResponse.ok("Simulated GPS step executed along route", updated));
    }

    @PostMapping("/report-missed")
    public ResponseEntity<ApiResponse<Complaint>> reportMissed(@Valid @RequestBody MissedCollectionReport report) {
        Complaint complaint = trackingService.reportMissedCollection(report);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Missed collection grievance submitted. Ticket ID: " + complaint.getTicketId(), complaint));
    }
}
