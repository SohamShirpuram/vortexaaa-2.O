package com.govfix.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "garbage_vans")
public class GarbageVan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 30)
    private String vehicleNumber;

    @Column(name = "driver_name", nullable = false, length = 100)
    private String driverName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "assigned_zone", nullable = false, length = 50)
    private String assignedZone;

    @Column(name = "current_latitude", nullable = false)
    private Double currentLatitude;

    @Column(name = "current_longitude", nullable = false)
    private Double currentLongitude;

    private Double heading = 0.0;

    @Column(name = "speed_kmph")
    private Double speedKmph = 18.0;

    @Column(length = 30)
    private String status = "ON_DUTY"; // ON_DUTY, IDLE, COMPLETED

    @OneToMany(mappedBy = "van", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("stopOrder ASC")
    @JsonIgnoreProperties("van")
    private List<GarbageVanStop> stops = new ArrayList<>();

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    public GarbageVan() {}

    public GarbageVan(String vehicleNumber, String driverName, String contactPhone, String assignedZone, Double currentLatitude, Double currentLongitude, Double heading, Double speedKmph, String status) {
        this.vehicleNumber = vehicleNumber;
        this.driverName = driverName;
        this.contactPhone = contactPhone;
        this.assignedZone = assignedZone;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.heading = heading;
        this.speedKmph = speedKmph;
        this.status = status;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getAssignedZone() { return assignedZone; }
    public void setAssignedZone(String assignedZone) { this.assignedZone = assignedZone; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }

    public Double getSpeedKmph() { return speedKmph; }
    public void setSpeedKmph(Double speedKmph) { this.speedKmph = speedKmph; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<GarbageVanStop> getStops() { return stops; }
    public void setStops(List<GarbageVanStop> stops) { this.stops = stops; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}

