package org.aiknowledge.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}