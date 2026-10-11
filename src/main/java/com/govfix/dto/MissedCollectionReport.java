package com.govfix.dto;

import jakarta.validation.constraints.NotBlank;

public class MissedCollectionReport {

    private String vehicleNumber;

    @NotBlank(message = "Locality is required")
    private String locality;

    private String landmark;

    @NotBlank(message = "Citizen name is required")
    private String citizenName;

    @NotBlank(message = "Contact number is required")
    private String citizenContact;

    private String notes;

    public MissedCollectionReport() {}

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getCitizenContact() { return citizenContact; }
    public void setCitizenContact(String citizenContact) { this.citizenContact = citizenContact; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

