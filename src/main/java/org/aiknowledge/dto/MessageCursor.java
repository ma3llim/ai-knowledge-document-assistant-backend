package org.aiknowledge.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record MessageCursor(
        Instant beforeCreatedAt,
        UUID beforeMessageId
) {
}
