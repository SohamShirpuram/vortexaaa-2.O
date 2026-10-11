package com.govfix.dto;

public class AiSuggestionResponse {
    private Long recommendationId;
    private String suggestedCategory;
    private String suggestedDepartment;
    private String suggestedPriority;
    private Double confidenceScore;
    private String explanation;
    private String engineUsed; // "RULE_BASED_NLP" or "GEMINI_AI"

    public AiSuggestionResponse() {}

    public AiSuggestionResponse(String suggestedCategory, String suggestedDepartment, String suggestedPriority, Double confidenceScore, String explanation, String engineUsed) {
        this.suggestedCategory = suggestedCategory;
        this.suggestedDepartment = suggestedDepartment;
        this.suggestedPriority = suggestedPriority;
        this.confidenceScore = confidenceScore;
        this.explanation = explanation;
        this.engineUsed = engineUsed;
    }

    public Long getRecommendationId() { return recommendationId; }
    public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }

    public String getSuggestedCategory() { return suggestedCategory; }
    public void setSuggestedCategory(String suggestedCategory) { this.suggestedCategory = suggestedCategory; }

    public String getSuggestedDepartment() { return suggestedDepartment; }
    public void setSuggestedDepartment(String suggestedDepartment) { this.suggestedDepartment = suggestedDepartment; }

    public String getSuggestedPriority() { return suggestedPriority; }
    public void setSuggestedPriority(String suggestedPriority) { this.suggestedPriority = suggestedPriority; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getEngineUsed() { return engineUsed; }
    public void setEngineUsed(String engineUsed) { this.engineUsed = engineUsed; }
}
