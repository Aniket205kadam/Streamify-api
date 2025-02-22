package com.streamify.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${application.Security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.Security.jwt.secret-key}")
    private String secretKey;

    public String generateJwtToken(Map<String, Object> claims, UserDetails userDetails) {
        return buildJwtToken(claims, userDetails, jwtExpiration);
    }

    private String buildJwtToken(Map<String, Object> claims, UserDetails userDetails, long jwtExpiration) {
        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts
                .builder()
                .setSubject(claims.get("username").toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .claim("authorities", authorities)
                .setIssuer("Streamify.com")
                .signWith(getSignKey())
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        final String identifier = extractIdentifier(jwtToken);
        return !isTokenExpired(jwtToken);
    }

    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date(System.currentTimeMillis()));
    }

    private Date extractExpiration(String jwtToken) {
        return extractClaims(jwtToken, Claims::getExpiration);
    }

    private <T> T extractClaims(String jwtToken, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwtToken) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    public String extractIdentifier(String jwtToken) {
        return extractClaims(jwtToken, Claims::getSubject);
    }
}
