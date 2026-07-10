package com.hospital.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtService {
    private final SecretKey secretKey;
    private final long ttlSeconds;

    public JwtService(String secret, long ttlSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String createToken(DoctorPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .subject(String.valueOf(principal.userId()))
                .claim("doctorId", principal.doctorId())
                .claim("deptId", principal.deptId())
                .claim("doctorType", principal.doctorType())
                .claim("roleCode", principal.roleCode())
                .claim("realName", principal.realName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public DoctorPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new DoctorPrincipal(
                Long.valueOf(claims.getSubject()),
                claims.get("doctorId", Long.class),
                claims.get("deptId", Long.class),
                claims.get("doctorType", String.class),
                claims.get("roleCode", String.class),
                claims.get("realName", String.class)
        );
    }
}
