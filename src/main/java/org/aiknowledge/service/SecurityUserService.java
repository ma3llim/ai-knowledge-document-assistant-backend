package org.aiknowledge.service;

import org.aiknowledge.config.security.OAuth.CustomOidcUser;
import org.aiknowledge.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityUserService {
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }

        if (principal instanceof CustomOidcUser customOidcUser) {
            return customOidcUser.getUserId();
        }
        
        throw new IllegalStateException("Authenticated user information is unavailable");
    }
}
