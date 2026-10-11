package com.govfix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComplaintCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Locality is required")
    private String locality;

    private String wardOrZone;
    private String landmark;
    private String priority = "MEDIUM";
    private String citizenName;
    private String citizenContact;
    private String photoUrl;

    public ComplaintCreateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getWardOrZone() { return wardOrZone; }
    public void setWardOrZone(String wardOrZone) { this.wardOrZone = wardOrZone; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getCitizenContact() { return citizenContact; }
    public void setCitizenContact(String citizenContact) { this.citizenContact = citizenContact; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
