package com.custom.payment.security.jwt;


import com.custom.payment.db.projection.UserAuthProjection;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtTokenService {

    private final SecretKey accessTokenSecretKey;

    private final long accessTokenValidity = 1000 * 60 * 15; // 15 минут

    public JwtTokenService(@Value("${jwt.secret}") String accessTokenSecretKey) {
        this.accessTokenSecretKey = Keys.hmacShaKeyFor(accessTokenSecretKey.getBytes());
    }

    public String getGenerateAccessToken(UserAuthProjection user) {

        if (user == null) {
            throw new IllegalArgumentException("User not found for token generation");
        }

        return Jwts.builder()
                .subject(user.getLogin())
                .claim("user_id", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(accessTokenSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        return validateSign(token)
                && !isTokenExpired(token);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims.get("user_id", Long.class);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(accessTokenSecretKey)
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }

    private Date getExpirationDateFromToken(String token) {
        return extractClaims(token).getExpiration();
    }

    private boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }


    private boolean validateSign(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(accessTokenSecretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
