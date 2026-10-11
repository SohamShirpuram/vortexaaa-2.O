package com.govfix.controller;

import com.govfix.dto.ApiResponse;
import com.govfix.entity.AuditEvent;
import com.govfix.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditEvent>>> getAllAuditLogs(
            @RequestParam(required = false) String ticketId) {

        List<AuditEvent> logs = (ticketId != null && !ticketId.isBlank())
                ? auditService.getAuditLogsForTicket(ticketId)
                : auditService.getAllAuditLogs();

        return ResponseEntity.ok(ApiResponse.ok("Audit records retrieved", logs));
    }
}
