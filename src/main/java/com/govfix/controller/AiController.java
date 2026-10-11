package com.govfix.controller;

import com.govfix.dto.AiDecisionRequest;
import com.govfix.dto.AiSuggestionRequest;
import com.govfix.dto.AiSuggestionResponse;
import com.govfix.dto.ApiResponse;
import com.govfix.entity.AiRecommendation;
import com.govfix.service.AIRecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AIRecommendationService aiRecommendationService;

    public AiController(AIRecommendationService aiRecommendationService) {
        this.aiRecommendationService = aiRecommendationService;
    }

    @PostMapping("/suggest-category")
    public ResponseEntity<ApiResponse<AiSuggestionResponse>> suggestCategory(
            @Valid @RequestBody AiSuggestionRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "officer_sharma";
        String role = auth != null && !auth.getAuthorities().isEmpty()
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "OFFICER";

        AiSuggestionResponse suggestion = aiRecommendationService.generateRecommendation(request, username, role);
        return ResponseEntity.ok(ApiResponse.ok("AI recommendation generated", suggestion));
    }

    @PostMapping("/decision")
    public ResponseEntity<ApiResponse<AiRecommendation>> recordDecision(
            @Valid @RequestBody AiDecisionRequest request,
            Authentication auth) {

        String username = auth != null ? auth.getName() : "officer_sharma";
        String role = auth != null && !auth.getAuthorities().isEmpty()
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : "OFFICER";

        AiRecommendation decision = aiRecommendationService.processHumanDecision(request, username, role);
        return ResponseEntity.ok(ApiResponse.ok("Human review applied to AI recommendation: " + request.getDecision(), decision));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<AiRecommendation>>> getPendingRecommendations() {
        List<AiRecommendation> list = aiRecommendationService.getPendingRecommendations();
        return ResponseEntity.ok(ApiResponse.ok("Pending AI recommendations awaiting human review", list));
    }
}

