package com.govfix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "garbage_van_stops")
public class GarbageVanStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "van_id", nullable = false)
    @JsonIgnore
    private GarbageVan van;

    @Column(name = "locality_name", nullable = false, length = 100)
    private String localityName;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "scheduled_arrival_time", length = 20)
    private String scheduledArrivalTime;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "estimated_arrival_minutes")
    private Integer estimatedArrivalMinutes = 15;

    public GarbageVanStop() {}

    public GarbageVanStop(GarbageVan van, String localityName, Integer stopOrder, Double latitude, Double longitude, String scheduledArrivalTime, Boolean isCompleted, Integer estimatedArrivalMinutes) {
        this.van = van;
        this.localityName = localityName;
        this.stopOrder = stopOrder;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scheduledArrivalTime = scheduledArrivalTime;
        this.isCompleted = isCompleted;
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GarbageVan getVan() { return van; }
    public void setVan(GarbageVan van) { this.van = van; }

    public String getLocalityName() { return localityName; }
    public void setLocalityName(String localityName) { this.localityName = localityName; }

    public Integer getStopOrder() { return stopOrder; }
    public void setStopOrder(Integer stopOrder) { this.stopOrder = stopOrder; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getScheduledArrivalTime() { return scheduledArrivalTime; }
    public void setScheduledArrivalTime(String scheduledArrivalTime) { this.scheduledArrivalTime = scheduledArrivalTime; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Integer getEstimatedArrivalMinutes() { return estimatedArrivalMinutes; }
    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) { this.estimatedArrivalMinutes = estimatedArrivalMinutes; }
}
