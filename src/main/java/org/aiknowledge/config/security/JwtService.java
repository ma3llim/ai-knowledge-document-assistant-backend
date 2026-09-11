package org.aiknowledge.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.properties.JwtProperties;
import org.aiknowledge.entity.User;
import org.aiknowledge.enums.TokenType;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }

    public String generateToken(UUID userId, long expirationMillis, TokenType tokenType, UUID tokenId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(tokenId.toString())
                .subject(userId.toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMillis)))
                .claim("type", tokenType.name())
                .signWith(secretKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String generateAccessToken(User user) {
        return generateToken(user.getId(), jwtProperties.getAccessTokenExpiration(), TokenType.ACCESS, UUID.randomUUID());
    }

    public String generateRefreshToken(User user, String tokenId) {
        return generateToken(user.getId(), jwtProperties.getRefreshTokenExpiration(), TokenType.REFRESH, UUID.fromString(tokenId));
    }

    // parse + verify JWT
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(secretKey()).requireIssuer(jwtProperties.getIssuer()).build().parseSignedClaims(token);
    }

    // extract claims
    public Claims extractClaims(String token) {
        return parse(token).getPayload();
    }

    // extract UserId
    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    // extract Jwt Id
    public String getJwtId(Claims claims) {
        return claims.getId();
    }

    // check token type access
    public boolean isAccessToken(Claims claims) {
        return TokenType.ACCESS.name().equalsIgnoreCase(claims.get("type", String.class));
    }

    // check token type refresh
    public boolean isRefreshToken(Claims claims) {
        return TokenType.REFRESH.name().equalsIgnoreCase(claims.get("type", String.class));
    }

    // validate access token
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return isAccessToken(claims) && claims.getId() != null && claims.getSubject() != null;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    // validate refresh token
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return isRefreshToken(claims) && claims.getId() != null && claims.getSubject() != null;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
