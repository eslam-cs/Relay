package com.relay.iot.dto;

/**
 * Response containing the generated JWT token and expiration time.
 */
public class TokenResponse {
    private String token;
    private long expiresIn;

    public TokenResponse() {
    }

    public TokenResponse(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
