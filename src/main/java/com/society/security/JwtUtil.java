package com.society.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey(){

        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String generateToken(String phoneNo,String role,Long userId){
        Map<String, Object> claims=new HashMap<>();
        claims.put("role",role);
        claims.put("userId",userId);
        return Jwts.builder()
                .claims(claims)
                .subject(phoneNo)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractPhoneNo(String token){

        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims,T> resolver){
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    public String extractRole(String token){
        return extractClaim(token,claims -> claims.get("role",String.class));
    }

    public Long extractUserId(String token){
        return extractClaim(token,claims -> claims.get("userId",Long.class));
    }

    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }

    public boolean isTokenValid(String token, String phoneNo){
        try {
            return extractPhoneNo(token).equals(phoneNo) && !isTokenExpired(token);
        }catch (Exception exception){
            return false;
        }
    }

    private boolean isTokenExpired(String token){

        return extractExpiration(token).before(new Date());
    }

    private String createToken(Map<String, Object> claims, String subject){
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean validateToken(String token, String phoneNo){
        try {
            String extractedPhoneNo = extractPhoneNo(token);
            return (extractedPhoneNo.equals(phoneNo) && !isTokenExpired(token));
        }catch (JwtException | IllegalArgumentException exception){
            return false;
        }
    }




}
