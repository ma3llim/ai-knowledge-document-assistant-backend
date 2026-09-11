package org.aiknowledge.config.security.OAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.User;
import org.aiknowledge.entity.UserIdentity;
import org.aiknowledge.enums.AuthProvider;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.repository.UserIdentityRepository;
import org.aiknowledge.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOidcUserService extends OidcUserService {
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
        User user;

        if (userIdentity != null) {
            user = userRepository.findById(userIdentity.getUserId()).orElseThrow(() ->
                    new ResourceNotFoundException("User associated with Google identity was not found"));
        } else {
            user = userRepository.findByEmail(email).orElse(null);

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
        }

        log.info("Google authentication successful, userId={}", user.getId());
        Collection<? extends GrantedAuthority> authorities = oidcUser.getAuthorities();

        return new CustomOidcUser(
                authorities,
                oidcUser,
                user.getId()
        );
    }
}
