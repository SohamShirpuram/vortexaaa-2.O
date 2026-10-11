package com.govfix.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VanTrackingResponse {
    private Long id;
    private String vehicleNumber;
    private String driverName;
    private String contactPhone;
    private String assignedZone;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double heading;
    private Double speedKmph;
    private String status;
    private LocalDateTime lastUpdatedAt;
    private List<StopDto> stops;
    private Integer nextStopEtaMinutes;
    private String nextStopLocality;
    private boolean isSimulated = true;

    public static class StopDto {
        private Long id;
        private String localityName;
        private Integer stopOrder;
        private Double latitude;
        private Double longitude;
        private String scheduledArrivalTime;
        private Boolean isCompleted;
        private Integer estimatedArrivalMinutes;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
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

    public VanTrackingResponse() {}

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

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    public List<StopDto> getStops() { return stops; }
    public void setStops(List<StopDto> stops) { this.stops = stops; }

    public Integer getNextStopEtaMinutes() { return nextStopEtaMinutes; }
    public void setNextStopEtaMinutes(Integer nextStopEtaMinutes) { this.nextStopEtaMinutes = nextStopEtaMinutes; }

    public String getNextStopLocality() { return nextStopLocality; }
    public void setNextStopLocality(String nextStopLocality) { this.nextStopLocality = nextStopLocality; }

    public boolean isSimulated() { return isSimulated; }
    public void setSimulated(boolean simulated) { isSimulated = simulated; }
}
