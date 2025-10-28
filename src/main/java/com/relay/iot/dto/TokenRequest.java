package com.relay.iot.dto;

/**
 * Request body for token generation.
 * Optional fields can be used to identify the requester.
 */
public class TokenRequest {
    private String deviceId;
    private String deviceType;

    public TokenRequest() {
    }

    public TokenRequest(String deviceId, String deviceType) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
