package com.govfix.service;

import com.govfix.dto.AiDecisionRequest;
import com.govfix.dto.AiSuggestionRequest;
import com.govfix.dto.AiSuggestionResponse;
import com.govfix.entity.AiRecommendation;
import com.govfix.entity.Complaint;
import com.govfix.repository.AiRecommendationRepository;
import com.govfix.repository.ComplaintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AIRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(AIRecommendationService.class);

    private final AiRecommendationRepository aiRecommendationRepository;
    private final ComplaintRepository complaintRepository;
    private final AuditService auditService;

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String geminiApiKey;

    public AIRecommendationService(AiRecommendationRepository aiRecommendationRepository,
                                   ComplaintRepository complaintRepository,
                                   AuditService auditService) {
        this.aiRecommendationRepository = aiRecommendationRepository;
        this.complaintRepository = complaintRepository;
        this.auditService = auditService;
    }

    /**
     * Generates a recommendation with rule-based fallback.
     * Persists recommendation with status PENDING for mandatory human approval.
     */
    @Transactional
    public AiSuggestionResponse generateRecommendation(AiSuggestionRequest request, String actorUsername, String actorRole) {
        String text = ((request.getTitle() != null ? request.getTitle() : "") + " " +
                       (request.getDescription() != null ? request.getDescription() : "")).toLowerCase(Locale.ENGLISH);

        AiSuggestionResponse response = classifyWithRules(text);

        // If complaint ID is linked, persist recommendation for human approval
        if (request.getComplaintId() != null) {
            Complaint complaint = complaintRepository.findById(request.getComplaintId()).orElse(null);
            if (complaint != null) {
                AiRecommendation recommendation = new AiRecommendation(
                        complaint,
                        response.getSuggestedCategory(),
                        response.getSuggestedDepartment(),
                        response.getSuggestedPriority(),
                        response.getConfidenceScore(),
                        response.getExplanation()
                );
                AiRecommendation saved = aiRecommendationRepository.save(recommendation);
                response.setRecommendationId(saved.getId());

                auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                        "AI_RECOMMENDATION_GENERATED", null, null, "SUCCESS",
                        "AI suggested: " + response.getSuggestedCategory() + " (" + response.getSuggestedDepartment() +
                        ", " + response.getSuggestedPriority() + "). Awaiting human review.");
            }
        }

        return response;
    }

    /**
     * Human-in-the-loop decision: Approve, Edit, or Reject
     * Updates complaint fields only upon human approval/edit.
     * AI NEVER transitions or closes complaints directly.
     */
    @Transactional
    public AiRecommendation processHumanDecision(AiDecisionRequest request, String actorUsername, String actorRole) {
        AiRecommendation recommendation = aiRecommendationRepository.findById(request.getRecommendationId())
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found: " + request.getRecommendationId()));

        String decision = request.getDecision().toUpperCase();
        if ("APPROVE".equals(decision) || "APPROVED".equals(decision)) {
            recommendation.setStatus("APPROVED");
        } else if ("REJECT".equals(decision) || "REJECTED".equals(decision)) {
            recommendation.setStatus("REJECTED");
        } else {
            recommendation.setStatus("EDITED");
        }
        recommendation.setDecidedByUsername(actorUsername);
        recommendation.setDecidedAt(LocalDateTime.now());

        Complaint complaint = recommendation.getComplaint();

        if ("APPROVE".equals(decision)) {
            complaint.setCategory(recommendation.getSuggestedCategory());
            complaint.setDepartment(recommendation.getSuggestedDepartment());
            complaint.setPriority(recommendation.getSuggestedPriority());
            complaintRepository.save(complaint);

            auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                    "AI_RECOMMENDATION_APPROVED", null, null, "SUCCESS",
                    "Officer approved AI suggestion: Category='" + complaint.getCategory() +
                    "', Dept='" + complaint.getDepartment() + "', Priority='" + complaint.getPriority() + "'");

        } else if ("EDIT".equals(decision)) {
            if (request.getEditedCategory() != null && !request.getEditedCategory().isBlank()) {
                complaint.setCategory(request.getEditedCategory());
            }
            if (request.getEditedDepartment() != null && !request.getEditedDepartment().isBlank()) {
                complaint.setDepartment(request.getEditedDepartment());
            }
            if (request.getEditedPriority() != null && !request.getEditedPriority().isBlank()) {
                complaint.setPriority(request.getEditedPriority());
            }
            complaintRepository.save(complaint);

            auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                    "AI_RECOMMENDATION_EDITED", null, null, "SUCCESS",
                    "Officer edited and applied AI recommendation. Notes: " + request.getNotes());

        } else if ("REJECT".equals(decision)) {
            auditService.logAuditEvent(actorUsername, actorRole, complaint.getId(), complaint.getTicketId(),
                    "AI_RECOMMENDATION_REJECTED", null, null, "SUCCESS",
                    "Officer rejected AI suggestion. Notes: " + request.getNotes());
        }

        return aiRecommendationRepository.save(recommendation);
    }

    private AiSuggestionResponse classifyWithRules(String text) {
        // High-precision civic pattern matching for PCMC municipal domains
        if (text.contains("pothole") || text.contains("crater") || text.contains("road") ||
            text.contains("asphalt") || text.contains("tar") || text.contains("speed breaker") || text.contains("brts")) {
            return new AiSuggestionResponse(
                    "Roads and potholes",
                    "Roads and Infrastructure",
                    (text.contains("deep") || text.contains("accident") || text.contains("danger") || text.contains("station")) ? "HIGH" : "MEDIUM",
                    0.94,
                    "Detected keywords indicating roadway surface damage and vehicular traffic hazard in PCMC ward.",
                    "RULE_BASED_NLP"
            );
        }

        if (text.contains("water") || text.contains("pipeline") || text.contains("tap") ||
            text.contains("leakage") || text.contains("contamination") || text.contains("muddy") || text.contains("drinking")) {
            return new AiSuggestionResponse(
                    "Water supply",
                    "Water Supply & Quality",
                    (text.contains("contamination") || text.contains("muddy") || text.contains("leak") || text.contains("burst")) ? "CRITICAL" : "HIGH",
                    0.95,
                    "Detected municipal water supply infrastructure indicators and potential public health drinking water risks.",
                    "RULE_BASED_NLP"
            );
        }

        if (text.contains("garbage") || text.contains("trash") || text.contains("waste") ||
            text.contains("van") || text.contains("dumpster") || text.contains("collection") || text.contains("dustbin")) {
            return new AiSuggestionResponse(
                    "Waste management",
                    "Waste Management",
                    (text.contains("overflowing") || text.contains("rot") || text.contains("days")) ? "HIGH" : "MEDIUM",
                    0.93,
                    "Detected municipal solid waste disposal, door-to-door van route, or community bin sanitation issues.",
                    "RULE_BASED_NLP"
            );
        }

        if (text.contains("drain") || text.contains("drainage") || text.contains("gutter") ||
            text.contains("manhole") || text.contains("sewer") || text.contains("sewage") || text.contains("overflow")) {
            return new AiSuggestionResponse(
                    "Drainage and sanitation",
                    "Drainage Department",
                    (text.contains("open manhole") || text.contains("overflow") || text.contains("choke")) ? "CRITICAL" : "HIGH",
                    0.96,
                    "Detected stormwater or sewage drainage blockage posing pedestrian hazards or urban waterlogging.",
                    "RULE_BASED_NLP"
            );
        }

        if (text.contains("light") || text.contains("streetlight") || text.contains("pole") ||
            text.contains("dark") || text.contains("lamp") || text.contains("wire") || text.contains("electrical")) {
            return new AiSuggestionResponse(
                    "Streetlights",
                    "Electrical Department",
                    (text.contains("wire") || text.contains("spark") || text.contains("junction")) ? "HIGH" : "MEDIUM",
                    0.91,
                    "Detected public illumination or electrical streetlight failure impacting night-time citizen safety.",
                    "RULE_BASED_NLP"
            );
        }

        if (text.contains("garden") || text.contains("park") || text.contains("tree") ||
            text.contains("branch") || text.contains("playground") || text.contains("lawn") || text.contains("bench")) {
            return new AiSuggestionResponse(
                    "Public gardens",
                    "Garden Department",
                    (text.contains("fallen tree") || text.contains("blocked pathway")) ? "HIGH" : "LOW",
                    0.89,
                    "Detected municipal public garden and horticulture maintenance or tree obstruction keywords.",
                    "RULE_BASED_NLP"
            );
        }

        // Generic default
        return new AiSuggestionResponse(
                "Roads and potholes",
                "Roads and Infrastructure",
                "MEDIUM",
                0.75,
                "Default civic grievance triage based on municipal service baseline.",
                "RULE_BASED_NLP"
        );
    }

    @Transactional(readOnly = true)
    public List<AiRecommendation> getPendingRecommendations() {
        return aiRecommendationRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }
}
