package com.hashedin.huSpark.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime()/*System.currentTimeMillis()*/ + ((jwtExpirationInMs == 0)? 1000 * 60 * 60 * 10:jwtExpirationInMs));

        return Jwts.builder()
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("isAdmin", userPrincipal.isAdmin())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Updated method
                .compact();
    }

    public String getUserEmailFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Updated parser method
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // Updated parser method
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}