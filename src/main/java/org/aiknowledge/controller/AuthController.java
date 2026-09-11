package org.aiknowledge.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.dto.ApiSuccessResponse;
import org.aiknowledge.dto.request.OAuthExchangeRequestDto;
import org.aiknowledge.dto.response.UserAndTokenResponseDto;
import org.aiknowledge.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/oauth/exchange")
    public ResponseEntity<ApiSuccessResponse<UserAndTokenResponseDto>> exchangeOAuthCode(
            @Valid @RequestBody OAuthExchangeRequestDto request, HttpServletRequest httpServletRequest,
            HttpServletResponse response) {

        UserAndTokenResponseDto userAndTokenResponseDto = authenticationService.exchangeOAuthCode(request.code(), response);

        return ResponseEntity.ok(
                ApiSuccessResponse.<UserAndTokenResponseDto>builder()
                        .success(true)
                        .message("OAuth authentication successful")
                        .data(userAndTokenResponseDto)
                        .path(httpServletRequest.getRequestURI())
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiSuccessResponse<UserAndTokenResponseDto>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        UserAndTokenResponseDto result = authenticationService.refreshToken(refreshToken, response);

        return ResponseEntity.ok(ApiSuccessResponse.<UserAndTokenResponseDto>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(result)
                .path(request.getRequestURI())
                .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        authenticationService.logout(refreshToken, response);

        return ResponseEntity.noContent().build();
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (var cookie : request.getCookies()) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
