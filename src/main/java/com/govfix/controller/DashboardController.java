package com.govfix.controller;

import com.govfix.dto.ApiResponse;
import com.govfix.dto.DashboardStats;
import com.govfix.service.ComplaintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ComplaintService complaintService;

    public DashboardController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        DashboardStats stats = complaintService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Dashboard metrics retrieved", stats));
    }
}

