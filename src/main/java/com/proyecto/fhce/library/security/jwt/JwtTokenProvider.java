package com.proyecto.fhce.library.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.security.CustomUserDetailsService;
import com.proyecto.fhce.library.security.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtTokenProvider {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expiration}")
  private long jwtExpirationMs;
  private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

  // 🔐 Generar token
  public String generateToken(Authentication authentication) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    // String username = authentication.getName();
    // String username = userPrincipal.getUsername();

    List<String> roles = userPrincipal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

    Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    return Jwts.builder()
        .setSubject(userPrincipal.getUsername())
        // .setSubject(username)
        .claim("roles", roles)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateTokenFromUsername(String username) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
    Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String getUsernameFromJwt(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
        .build()
        .parseClaimsJws(token)
        .getBody();

    return claims.getSubject();
  }

  // 🔍 Obtener username desde token
  public String getUsernameFromToken(String token) {
    return getClaims(token).getSubject();
  }

  // 🔍 Obtener roles desde token
  public List<String> getRolesFromToken(String token) {
    // return getClaims(token).get("roles", List.class);
    List<?> roles = (List<?>) getClaims(token).get("roles");

    return roles.stream()
        .map(String::valueOf)
        .toList();
  }

  // ✅ Validar token
  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (SignatureException | MalformedJwtException ex) {
      logger.error("Invalid JWT token: {}", ex.getMessage());
    } catch (ExpiredJwtException ex) {
      logger.error("Expired JWT token: {}", ex.getMessage());
    } catch (UnsupportedJwtException ex) {
      logger.error("Unsupported JWT token: {}", ex.getMessage());
    } catch (IllegalArgumentException ex) {
      logger.error("JWT claims string is empty: {}", ex.getMessage());
    }
    return false;
  }

  public boolean validateTokenAndEnabled(String token, CustomUserDetailsService userDetailsService) {
    if (!validateToken(token))
      return false;

    String username = getUsernameFromJwt(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    return userDetails.isEnabled();
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