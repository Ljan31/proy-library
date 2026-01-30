package com.proyecto.fhce.library.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expiration}")
  private long jwtExpirationMs;

  // 🔐 Generar token
  public String generateToken(Authentication authentication) {

    String username = authentication.getName();

    List<String> roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

    Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    return Jwts.builder()
        .setSubject(username)
        .claim("roles", roles)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  // 🔍 Obtener username desde token
  public String getUsernameFromToken(String token) {
    return getClaims(token).getSubject();
  }

  // 🔍 Obtener roles desde token
  public List<String> getRolesFromToken(String token) {
    return getClaims(token).get("roles", List.class);
  }

  // ✅ Validar token
  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  // 🧠 Claims comunes
  private Claims getClaims(String token) {
    Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}