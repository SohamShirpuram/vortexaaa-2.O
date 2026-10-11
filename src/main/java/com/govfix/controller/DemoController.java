package com.govfix.controller;

import com.govfix.dto.ApiResponse;
import com.govfix.entity.Escalation;
import com.govfix.entity.User;
import com.govfix.repository.UserRepository;
import com.govfix.service.DataInitializerService;
import com.govfix.service.EscalationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final EscalationService escalationService;
    private final DataInitializerService dataInitializerService;
    private final UserRepository userRepository;

    public DemoController(EscalationService escalationService,
                          DataInitializerService dataInitializerService,
                          UserRepository userRepository) {
        this.escalationService = escalationService;
        this.dataInitializerService = dataInitializerService;
        this.userRepository = userRepository;
    }

    @PostMapping("/advance-time")
    public ResponseEntity<ApiResponse<Map<String, Object>>> advanceTime(
            @RequestBody(required = false) Map<String, Integer> payload) {

        int hours = (payload != null && payload.containsKey("hours")) ? payload.get("hours") : 24;
        List<Escalation> newEscalations = escalationService.advanceSimulatedTime(hours);

        return ResponseEntity.ok(ApiResponse.ok("Simulated time advanced by " + hours + " hours [Demo Mode]", Map.of(
                "advancedHours", hours,
                "newEscalationsTriggered", newEscalations.size(),
                "escalations", newEscalations
        )));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> resetDemo() {
        dataInitializerService.resetDemoData();
        return ResponseEntity.ok(ApiResponse.ok("Demo environment completely reset to fresh pristine seed state.", "READY"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getDemoUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Available demo accounts", users));
    }
}

