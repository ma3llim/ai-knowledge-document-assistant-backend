package org.aiknowledge.config.security.OAuth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.properties.JwtProperties;
import org.aiknowledge.config.security.JwtService;
import org.aiknowledge.entity.User;
import org.aiknowledge.enums.TokenType;
import org.aiknowledge.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String email = oidcUser.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow();
        String accessToken = jwtService.generateToken(user.getId(), jwtProperties.getAccessTokenExpiration(), TokenType.ACCESS);
        String refreshToken = jwtService.generateToken(user.getId(), jwtProperties.getRefreshTokenExpiration(), TokenType.REFRESH);

        // TODO: Temporary approach for development.
        // Production frontend integration should use a secure
        // token transport strategy.
        response.sendRedirect(
                "http://localhost:5173/oauth/callback"
                        + "?accessToken=" + accessToken
                        + "&refreshToken=" + refreshToken
        );
    }
}
