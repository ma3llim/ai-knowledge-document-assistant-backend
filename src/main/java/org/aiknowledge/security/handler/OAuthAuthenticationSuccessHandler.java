package org.aiknowledge.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.Constants;
import org.aiknowledge.security.CustomOidcUser;
import org.aiknowledge.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOidcUser oidcUser = (CustomOidcUser) authentication.getPrincipal();
        UUID userId = oidcUser.getUserId();

        String oneTimeCode = authService.createOAuthLoginCode(userId);

        String redirectUrl = UriComponentsBuilder.fromUriString(Constants.FRONTEND_OAUTH_CALLBACK_URL).queryParam(
                "code", oneTimeCode).build().toUriString();

        log.info("OAuth login successful, redirecting userId={}", userId);
        response.sendRedirect(redirectUrl);
    }
}
