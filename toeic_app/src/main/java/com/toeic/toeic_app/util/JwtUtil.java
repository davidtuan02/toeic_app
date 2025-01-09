package com.toeic.toeic_app.util;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;


public class
JwtUtil {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String generateToken(String email) {
        long expirationTimeInMillis = 1000 * 60 * 60; // 1 giờ (3600000ms)
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTimeInMillis);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now) // Thời điểm phát hành
                .setExpiration(expirationDate) // Thời gian hết hạn
                .signWith(key) // Use the secure key
                .compact();
    }

    // Lấy thông tin user từ jwt
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

//    public boolean validateToken(String authToken) {
//        try {
//            Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
//            return true;
//        } catch (MalformedJwtException ex) {
//            log.error("Invalid JWT token");
//        } catch (ExpiredJwtException ex) {
//            log.error("Expired JWT token");
//        } catch (UnsupportedJwtException ex) {
//            log.error("Unsupported JWT token");
//        } catch (IllegalArgumentException ex) {
//            log.error("JWT claims string is empty.");
//        }
//        return false;
//    }
}

