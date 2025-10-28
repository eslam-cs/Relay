package com.relay.iot.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "readings", indexes = {
    @Index(name = "idx_sensor_timestamp", columnList = "sensorId,timestamp"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class Reading {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sensorId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;
    
    @Column(nullable = false)
    private Double value;
    
    @Column(nullable = false)
    private Instant timestamp;
    
    public Reading() {
    }
    
    public Reading(String sensorId, DeviceType deviceType, Double value, Instant timestamp) {
        this.sensorId = sensorId;
        this.deviceType = deviceType;
        this.value = value;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSensorId() {
        return sensorId;
    }
    
    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
    
    public DeviceType getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
