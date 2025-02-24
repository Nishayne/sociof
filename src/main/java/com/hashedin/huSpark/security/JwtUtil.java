package com.hashedin.huSpark.security;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET_KEY ="";
    private final long EXPIRATION_TIME=86400000;

    public String generateToken(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                .signsWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    public String extractEmail(String token){
        return getClaims(token).getSubject();
    }
    public boolean validateToken(String token, String userEmail){
        return (userEmail.equals(extractEmail(token)) && !isTokenExpired(token));
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token){
        return getClaims(token).getExpiration().before(new Date());
    }
}
