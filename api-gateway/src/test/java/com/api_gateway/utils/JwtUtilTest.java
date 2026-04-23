package com.api_gateway.utils;


import com.api_gateway.utility.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Use the same key that is in application.yml
    private static final String TEST_SECRET =
            "5A7234753778214125442A472D4B6150645367566B59703373367639792442264D";

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_SECRET));
    }

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
    }

    // ── isValid ──────────────────────────────────────────────────────

    @Test
    @DisplayName("isValid — returns true for a well-formed, unexpired token")
    void isValid_validToken_returnsTrue() {
        String token = buildToken("42", "john@example.com", "CUSTOMER",
                new Date(System.currentTimeMillis() + 86_400_000L));

        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("isValid — returns false for an expired token")
    void isValid_expiredToken_returnsFalse() {
        String token = buildToken("42", "john@example.com", "CUSTOMER",
                new Date(System.currentTimeMillis() - 1000L)); // already expired

        assertThat(jwtUtil.isValid(token)).isFalse();
    }

    @Test
    @DisplayName("isValid — returns false for a randomly corrupted token")
    void isValid_malformedToken_returnsFalse() {
        assertThat(jwtUtil.isValid("this.is.not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("isValid — returns false for blank string")
    void isValid_blankToken_returnsFalse() {
        assertThat(jwtUtil.isValid("")).isFalse();
    }

    // ── Extract ──────────────────────────────────────────────────────

    @Test
    @DisplayName("extractUserId — returns the subject set in the token")
    void extractUserId_returnsCorrectId() {
        String token = buildToken("99", "jane@example.com", "ADMIN",
                new Date(System.currentTimeMillis() + 86_400_000L));

        assertThat(jwtUtil.extractUserId(token)).isEqualTo("99");
    }

    @Test
    @DisplayName("extractEmail — returns the email claim")
    void extractEmail_returnsCorrectEmail() {
        String token = buildToken("1", "alice@example.com", "CUSTOMER",
                new Date(System.currentTimeMillis() + 86_400_000L));

        assertThat(jwtUtil.extractEmail(token)).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("extractRole — returns the role claim")
    void extractRole_returnsCorrectRole() {
        String token = buildToken("1", "admin@example.com", "ADMIN",
                new Date(System.currentTimeMillis() + 86_400_000L));

        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    // ── Helper ───────────────────────────────────────────────────────

    private String buildToken(String subject, String email, String role, Date expiry) {
        return Jwts.builder()
                .claims(Map.of("email", email, "role", role))
                .subject(subject)
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }
}
