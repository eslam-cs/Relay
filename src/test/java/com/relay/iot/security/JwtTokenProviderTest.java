package com.relay.iot.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_TIME = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, EXPIRATION_TIME);
    }

    @Test
    void testGenerateToken_Success() {
        // Act
        String token = jwtTokenProvider.generateToken(null, null);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken(null, null);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() {
        // Arrange - Create provider with very short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(TEST_SECRET, 1L); // 1 millisecond

        String token = shortExpirationProvider.generateToken(null, null);

        // Act - Wait for token to expire
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = shortExpirationProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGetExpirationTime() {
        // Act
        long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertEquals(EXPIRATION_TIME, expirationTime);
    }

    @Test
    void testGenerateToken_MultipleCalls_GenerateDifferentTokens() {
        // Act
        String token1 = jwtTokenProvider.generateToken(null, null);
        
        // Wait to ensure different timestamp (tokens include iat - issued at time)
        try {
            Thread.sleep(1100); // Sleep > 1 second to ensure different timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtTokenProvider.generateToken(null, null);

        // Assert
        assertNotEquals(token1, token2, "Different tokens should be generated at different times");
    }

    @Test
    void testTokenStructure() {
        // Act
        String token = jwtTokenProvider.generateToken(null, null);
        String[] parts = token.split("\\.");

        // Assert
        assertEquals(3, parts.length, "JWT should have 3 parts: header.payload.signature");
        assertTrue(parts[0].length() > 0, "Header should not be empty");
        assertTrue(parts[1].length() > 0, "Payload should not be empty");
        assertTrue(parts[2].length() > 0, "Signature should not be empty");
    }
}
