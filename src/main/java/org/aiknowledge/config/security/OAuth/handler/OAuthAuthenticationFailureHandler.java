package org.aiknowledge.config.security.OAuth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.constant.OAuthConstants;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
public class OAuthAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.warn("OAuth authentication failed: {}", exception.getMessage());

        String redirectUrl = UriComponentsBuilder.fromUriString(OAuthConstants.FRONTEND_OAUTH_CALLBACK_URL)
                .queryParam("error", "OAUTH_AUTHENTICATION_FAILED").build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}