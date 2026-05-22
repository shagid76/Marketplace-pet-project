package com.marketplace.backend.security;

import com.marketplace.backend.model.Admin.ActionType;
import com.marketplace.backend.model.Admin.AdminAction;
import com.marketplace.backend.repository.AdminActionRepository;
import com.marketplace.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AdminActionRepository adminActionRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found by email: " + email));

        boolean banned = hasActiveAction(user.getId(), ActionType.BAN);
        boolean blocked = hasActiveAction(user.getId(), ActionType.BLOCK);

        boolean accountNonLocked = !banned && !blocked;
        boolean enabled = !banned;

        return new CustomUserDetails(user, accountNonLocked, enabled);
    }

    private boolean hasActiveAction(String targetId, ActionType actionType) {
        return adminActionRepository
                .findFirstByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc(targetId, actionType)
                .filter(this::isActive)
                .isPresent();
    }

    private boolean isActive(AdminAction action) {
        return action.getRevokedAt() == null
                && (action.getExpiresAt() == null || action.getExpiresAt().isAfter(Instant.now()));
    }
}