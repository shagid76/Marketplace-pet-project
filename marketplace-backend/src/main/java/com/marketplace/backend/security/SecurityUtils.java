package com.marketplace.backend.security;

import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.model.User.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {
    public static String getCurrentUserIdOrThrow() {
        return getCurrentUserId().orElseThrow(() -> new NotFoundException("User not found"));
    }

    public static Optional<String> getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(CustomUserDetails.class::isInstance)
                .map(CustomUserDetails.class::cast)
                .map(CustomUserDetails::getUser)
                .map(User::getId);
    }

    public static void updateSecurityContext(User user) {
        CustomUserDetails customUserDetails = new CustomUserDetails(user, true, true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities()
                )
        );
    }
}
