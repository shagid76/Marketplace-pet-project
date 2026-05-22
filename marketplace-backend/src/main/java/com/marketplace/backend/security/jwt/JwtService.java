package com.marketplace.backend.security.jwt;

import com.marketplace.backend.dto.jwt.JwtAuthenticationDto;
import com.marketplace.backend.model.User.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.codec.Hex;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    public enum TokenType {
        ACCESS, REFRESH
    }

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationDto generateTokenPair(String email, String userId, Set<Role> roles) {
        JwtAuthenticationDto jwtDto = new JwtAuthenticationDto();
        jwtDto.setToken(generateToken(email, userId, roles));
        jwtDto.setRefreshToken(generateRefreshToken(email, userId, roles));
        return jwtDto;
    }

    public Optional<Claims> validateAndExtract(String token) {
        try {
            return Optional.of(extractAllClaims(token));

        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT");

        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT", e);

        } catch (MalformedJwtException e) {
            log.error("Malformed JWT", e);

        } catch (SecurityException e) {
            log.error("Invalid JWT signature", e);

        } catch (Exception e) {
            log.error("Invalid JWT", e);
        }

        return Optional.empty();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignedKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String getTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public String getUserIdFromToken(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    public String generateToken(String email, String userId, Set<Role> roles) {
        Date expiry = Date.from(
                LocalDateTime.now().plusMinutes(30)
                        .atZone(ZoneId.systemDefault()).toInstant()
        );
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", TokenType.ACCESS.name())
                .setExpiration(expiry)
                .signWith(getSignedKey())
                .compact();
    }

    public String generateRefreshToken(String email, String userId, Set<Role> roles) {
        Date expiry = Date.from(
                LocalDateTime.now().plusDays(5)
                        .atZone(ZoneId.systemDefault()).toInstant()
        );
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("roles", roles)
                .claim("type", TokenType.REFRESH.name())
                .setExpiration(expiry)
                .signWith(getSignedKey())
                .compact();
    }

    private SecretKey getSignedKey() {
        byte[] key = Hex.decode(jwtSecret);
        return Keys.hmacShaKeyFor(key);
    }
}