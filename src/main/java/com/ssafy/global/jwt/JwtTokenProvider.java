package com.ssafy.global.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final Key secretKey = Keys.hmacShaKeyFor("MY_SUPER_SECRET_KEY_1234567890_ABCDE".getBytes());
    private final long EXPIRATION = 1000L * 60 * 60 * 24; // 24시간

    public String createToken(Long userId, String email) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(email) // subject = 식별용 이메일로 변경
                .claim("userId", userId)  // PK는 claim으로
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
    	Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("email", String.class);
    }
}
