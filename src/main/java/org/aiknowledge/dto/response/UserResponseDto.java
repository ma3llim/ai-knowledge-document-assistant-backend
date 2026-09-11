package org.aiknowledge.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponseDto(
        UUID id,
        String email,
        String name,
        String profileImageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
