package com.school.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String TEST_SECRET =
            "7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, 7_200_000L, 604_800_000L);
    }

    @Test
    void generateAccessToken_containsExpectedClaims() {
        String token = jwtUtil.generateAccessToken(1L, "admin", "ADMIN");

        Claims claims = jwtUtil.parseToken(token);
        assertEquals("admin", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("ADMIN", claims.get("role", String.class));
        assertEquals(JwtUtil.TOKEN_TYPE_ACCESS, claims.get(JwtUtil.CLAIM_TOKEN_TYPE, String.class));
    }

    @Test
    void generateRefreshToken_containsExpectedClaims() {
        String token = jwtUtil.generateRefreshToken(2L, "teacher1", "TEACHER");

        Claims claims = jwtUtil.parseToken(token);
        assertEquals(JwtUtil.TOKEN_TYPE_REFRESH, claims.get(JwtUtil.CLAIM_TOKEN_TYPE, String.class));
    }

    @Test
    void parseTokenSafely_returnsNullForMalformedToken() {
        assertNull(jwtUtil.parseTokenSafely("not-a-jwt"));
    }

    @Test
    void parseTokenSafely_returnsNullForTamperedToken() {
        String token = jwtUtil.generateAccessToken(1L, "admin", "ADMIN");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertNull(jwtUtil.parseTokenSafely(tampered));
    }

    @Test
    void isValidToken_returnsTrueForValidAccessToken() {
        String token = jwtUtil.generateAccessToken(1L, "admin", "ADMIN");
        assertTrue(jwtUtil.isValidToken(token));
    }

    @Test
    void isValidToken_returnsFalseForInvalidToken() {
        assertFalse(jwtUtil.isValidToken("invalid"));
    }
}
