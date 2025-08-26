package com.gym.authservice.Security.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${spring.secret.key}")
    private String secretKey;
    private final long expiration = 1000 * 60 * 60;

    private SecretKey getKey() {
        byte[] bytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(String email, String role) {
        return
                Jwts.builder()
                        .setSubject(email)
                        .claim("role", role)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + expiration))
                        .signWith(getKey())
                        .compact();
    }

    public Claims extractAllClaims(String token) {
        return
                Jwts
                        .parserBuilder()
                        .setSigningKey(getKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date getExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    public boolean isValidToken(String token) {
        try {
            return !isExpired(token) && extractAllClaims(token) != null;
        } catch (JwtException e) {
            return false;
        }
    }
}
