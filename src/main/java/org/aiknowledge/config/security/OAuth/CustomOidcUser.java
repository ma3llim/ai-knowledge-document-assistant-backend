package org.aiknowledge.config.security.OAuth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.UUID;

@Getter
public class CustomOidcUser extends DefaultOidcUser {
    private final UUID userId;

    public CustomOidcUser(Collection<? extends GrantedAuthority> authorities, OidcUser oidcUser, UUID userId) {
        super(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        this.userId = userId;
    }
}