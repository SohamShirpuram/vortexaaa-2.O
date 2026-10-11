package com.govfix.controller;

import com.govfix.dto.ApiResponse;
import com.govfix.entity.Escalation;
import com.govfix.service.EscalationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/escalations")
public class EscalationController {

    private final EscalationService escalationService;

    public EscalationController(EscalationService escalationService) {
        this.escalationService = escalationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Escalation>>> getAllEscalations() {
        List<Escalation> list = escalationService.getAllEscalations();
        return ResponseEntity.ok(ApiResponse.ok("Escalated grievances retrieved", list));
    }

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<Escalation>> manualEscalate(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        Long complaintId = Long.valueOf(body.get("complaintId").toString());
        String reason = body.getOrDefault("reason", "Manual escalation by supervisor").toString();
        String username = auth != null ? auth.getName() : "supervisor_kulkarni";
        String role = auth != null && !auth.getAuthorities().isEmpty()
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "SUPERVISOR";

        Escalation esc = escalationService.manualEscalate(complaintId, reason, username, role);
        return ResponseEntity.ok(ApiResponse.ok("Complaint manually escalated to supervisor review", esc));
    }
}
