package org.aiknowledge.dto.response;


public record UserAndTokenResponseDto(
        String accessToken,
        UserResponseDto user
) {
}
