package com.inn.cafe.JWT;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtUtil {

    private static final long EXPIRY_MILLIS = 1000L * 60 * 60 * 24;

    // Was a string literal in source. Anyone who could read the repository could sign a
    // token for any email with role=admin. It now comes from the environment, and the app
    // refuses to start without a usable one rather than silently falling back to a default.
    @Value("${app.jwt.secret:}")
    private String secret;

    private byte[] key;

    @PostConstruct
    void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set to a random string of at least 32 characters "
                            + "(e.g. `openssl rand -base64 48`).");
        }
        key = secret.getBytes(StandardCharsets.UTF_8);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        // Informational only (lets the FE pick a landing route before /user/profile
        // returns). Authorisation never trusts this claim - see JwtFilter.
        claims.put("role", role);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRY_MILLIS))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }
}
