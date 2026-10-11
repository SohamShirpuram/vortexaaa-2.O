package com.govfix.controller;

import com.govfix.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        return ResponseEntity.ok(ApiResponse.ok("GovFix Civic Service Engine is healthy", Map.of(
                "status", "UP",
                "system", "GovFix - PCMC Citizen Grievance & Tracking Platform",
                "version", "1.0.0",
                "timestamp", System.currentTimeMillis()
        )));
    }
}

