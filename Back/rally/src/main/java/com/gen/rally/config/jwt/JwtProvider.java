package com.gen.rally.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private Key key;

    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    private final long ACCESS_TOKEN = 1000 * 60 * 60; // 1시간
    private final long REFRESH_TOKEN = 1000 * 60 * 60 * 24 * 7; // 일주일

    // 토큰 생성
    public String generateAccessToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ ACCESS_TOKEN))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    // 리프레시 토큰 생성
    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 사용자의 id 가져오기
    public String extractSubject(String token){
        return getClaims(token).getSubject();
    }

    // 토큰 검증 메서드
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token); // 여기서 예외가 발생하면 유효하지 않음
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature", e);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty or null", e);
        }
        return false;
    }

    // JWT에서 Claims 추출
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
