package com.relay.iot.controller;

import com.relay.iot.dto.TokenResponse;
import com.relay.iot.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for JWT token generation.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "JWT token generation endpoints")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/token")
    @Operation(
            summary = "Generate JWT token",
            description = "Generates a general JWT token for API authentication. No request body or credentials required."
    )
    @SecurityRequirements // No security required for this endpoint
    public ResponseEntity<TokenResponse> generateToken() {
        String token = jwtTokenProvider.generateToken(null, null);
        long expiresIn = jwtTokenProvider.getExpirationTime();

        TokenResponse response = new TokenResponse(token, expiresIn);
        return ResponseEntity.ok(response);
    }
}
