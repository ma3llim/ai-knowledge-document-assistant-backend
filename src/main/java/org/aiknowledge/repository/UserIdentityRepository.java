package org.aiknowledge.repository;

import org.aiknowledge.entity.UserIdentity;
import org.aiknowledge.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {
    Optional<UserIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<UserIdentity> findByUserId(UUID userId);
}
