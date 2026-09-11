package org.aiknowledge.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthExchangeRequestDto(
        @NotBlank(message = "OAuth code is required")
        String code
) {
}
