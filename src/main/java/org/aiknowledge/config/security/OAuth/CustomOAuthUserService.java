package org.aiknowledge.config.security.OAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.User;
import org.aiknowledge.entity.UserIdentity;
import org.aiknowledge.enums.AuthProvider;
import org.aiknowledge.repository.UserIdentityRepository;
import org.aiknowledge.repository.UserRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuthUserService extends OidcUserService {
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerUserId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String profileImageUrl = oidcUser.getPicture();
        AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());

        UserIdentity userIdentity = userIdentityRepository.findByProviderAndProviderUserId(authProvider, providerUserId).orElse(null);
        if (userIdentity != null) {
            return new CustomOidcUser(
                    oidcUser.getAuthorities(),
                    (DefaultOidcUser) oidcUser,
                    userIdentity.getUserId()
            );
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = User.builder().email(email)
                    .name(name)
                    .profileImageUrl(profileImageUrl)
                    .build();

            user = userRepository.save(user);
        }

        UserIdentity newIdentity = UserIdentity.builder()
                .userId(user.getId()).
                provider(authProvider)
                .providerUserId(providerUserId)
                .build();

        userIdentityRepository.save(newIdentity);

        return new CustomOidcUser(
                oidcUser.getAuthorities(),
                (DefaultOidcUser) oidcUser,
                user.getId()
        );
    }
}
