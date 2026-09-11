package org.aiknowledge.service;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.properties.CookieProperties;
import org.aiknowledge.config.properties.JwtProperties;
import org.aiknowledge.config.security.JwtService;
import org.aiknowledge.dto.response.UserAndTokenResponseDto;
import org.aiknowledge.dto.response.UserResponseDto;
import org.aiknowledge.entity.OAuthLoginCode;
import org.aiknowledge.entity.RefreshToken;
import org.aiknowledge.entity.User;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.exception.UnauthorizedException;
import org.aiknowledge.repository.OAuthLoginCodeRepository;
import org.aiknowledge.repository.RefreshTokenRepository;
import org.aiknowledge.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final OAuthLoginCodeRepository oauthLoginCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String createOAuthLoginCode(UUID userId) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawCode = HexFormat.of().formatHex(randomBytes);
        String codeHash = hash(rawCode);

        OAuthLoginCode loginCode = OAuthLoginCode.builder()
                .codeHash(codeHash)
                .userId(userId)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.getOAuthLoginCodeExpiration()))
                .build();

        oauthLoginCodeRepository.save(loginCode);

        return rawCode;
    }

    @Transactional
    public UserAndTokenResponseDto exchangeOAuthCode(String rawCode, HttpServletResponse response) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new UnauthorizedException("Invalid OAuth code");
        }

        String codeHash = hash(rawCode);

        OAuthLoginCode loginCode = oauthLoginCodeRepository.findByCodeHash(codeHash).orElseThrow(() ->
                new UnauthorizedException("Invalid OAuth code")
        );

        if (loginCode.isUsed()) {
            throw new UnauthorizedException("OAuth code has already been used");
        }

        if (loginCode.isExpired()) {
            throw new UnauthorizedException("OAuth code has expired");
        }

        loginCode.setUsedAt(Instant.now());
        oauthLoginCodeRepository.save(loginCode);

        User user = userRepository.findById(loginCode.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException("User not found"));

        UUID refreshTokenId = UUID.randomUUID();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenId.toString());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id(refreshTokenId)
                .userId(user.getId())
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpiration()))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        addRefreshTokenCookie(response, refreshToken);

        UserResponseDto userResponse = UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt()).build();

        return new UserAndTokenResponseDto(accessToken, userResponse);
    }

    @Transactional
    public UserAndTokenResponseDto refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Authentication is required");
        }

        if (!jwtService.validateRefreshToken(refreshToken)) {
            log.warn("Refresh token request failed: invalid token");
            throw new UnauthorizedException("Invalid refresh token");
        }

        Claims claims = jwtService.extractClaims(refreshToken);

        UUID tokenId = UUID.fromString(jwtService.getJwtId(claims));

        UUID userId = jwtService.getUserId(claims);

        RefreshToken tokenEntity = refreshTokenRepository.findById(tokenId).orElseThrow(() -> {
            log.warn("Refresh token not found, tokenId={}, userId={}", tokenId, userId);
            return new UnauthorizedException("Invalid refresh token");
        });

        if (!hash(refreshToken).equals(tokenEntity.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (Instant.now().isAfter(tokenEntity.getExpiresAt())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (!tokenEntity.getUserId().equals(userId)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (tokenEntity.isRevoked()) {
            log.warn("Refresh token reuse detected, userId={}", userId);
            throw new UnauthorizedException("Invalid refresh token");
        }

        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User not found"));

        UUID newTokenId = UUID.randomUUID();

        String newAccessToken = jwtService.generateAccessToken(user);

        String newRefreshToken = jwtService.generateRefreshToken(user, newTokenId.toString());

        tokenEntity.setRevoked(true);
        tokenEntity.setRevokedByTokenId(newTokenId);

        refreshTokenRepository.save(tokenEntity);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .id(newTokenId)
                .userId(userId)
                .refreshToken(newRefreshToken)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpiration()))
                .build();

        refreshTokenRepository.save(newRefreshTokenEntity);

        addRefreshTokenCookie(response, newRefreshToken);

        UserResponseDto userResponse = UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt()).build();

        return new UserAndTokenResponseDto(newAccessToken, userResponse);
    }

    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                if (jwtService.validateRefreshToken(refreshToken)) {
                    Claims claims = jwtService.extractClaims(refreshToken);

                    UUID tokenId = UUID.fromString(jwtService.getJwtId(claims));

                    refreshTokenRepository.findById(tokenId).ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
                }
            } catch (Exception exception) {
                log.debug("Logout token processing failed", exception);
            }
        }
        clearRefreshTokenCookie(response);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.getRefreshTokenName(), refreshToken)
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path(cookieProperties.getPath())
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.getRefreshTokenName(), "")
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path(cookieProperties.getPath())
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }
}
