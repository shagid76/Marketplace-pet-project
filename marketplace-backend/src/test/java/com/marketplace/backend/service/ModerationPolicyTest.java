package com.marketplace.backend.service;

import com.marketplace.backend.exception.AuthenticationException;
import com.marketplace.backend.model.Admin.ActionType;
import com.marketplace.backend.model.Admin.AdminAction;
import com.marketplace.backend.model.User.User;
import com.marketplace.backend.repository.AdminActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerationPolicyTest {

    @Mock AdminActionRepository adminActionRepository;

    @InjectMocks ModerationPolicy moderationPolicy;

    private User user(String id) {
        return User.builder().id(id).build();
    }

    // ── BAN ──────────────────────────────────────────────────────────────────

    @Test
    void validateUserAccess_bannedUser_throwsAuthenticationException() {
        User user = user("u-1");
        AdminAction ban = AdminAction.builder().id("a-1").actionType(ActionType.BAN).build();

        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-1", ActionType.BAN))
                .thenReturn(Optional.of(ban));

        assertThatThrownBy(() -> moderationPolicy.validateUserAccess(user))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("permanently banned");
    }

    @Test
    void validateUserAccess_noBan_doesNotThrow() {
        User user = user("u-2");

        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-2", ActionType.BAN))
                .thenReturn(Optional.empty());
        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-2", ActionType.BLOCK))
                .thenReturn(Optional.empty());

        assertThatCode(() -> moderationPolicy.validateUserAccess(user)).doesNotThrowAnyException();
    }

    // ── BLOCK ─────────────────────────────────────────────────────────────────

    @Test
    void validateUserAccess_activeBlock_throwsAuthenticationException() {
        User user = user("u-3");
        AdminAction block = AdminAction.builder()
                .id("a-2")
                .actionType(ActionType.BLOCK)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-3", ActionType.BAN))
                .thenReturn(Optional.empty());
        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-3", ActionType.BLOCK))
                .thenReturn(Optional.of(block));

        assertThatThrownBy(() -> moderationPolicy.validateUserAccess(user))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("temporarily blocked");
    }

    @Test
    void validateUserAccess_expiredBlock_doesNotThrow() {
        User user = user("u-4");
        AdminAction block = AdminAction.builder()
                .id("a-3")
                .actionType(ActionType.BLOCK)
                .expiresAt(Instant.now().minusSeconds(3600)) // already expired
                .build();

        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-4", ActionType.BAN))
                .thenReturn(Optional.empty());
        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-4", ActionType.BLOCK))
                .thenReturn(Optional.of(block));

        assertThatCode(() -> moderationPolicy.validateUserAccess(user)).doesNotThrowAnyException();
    }

    @Test
    void validateUserAccess_blockWithNullExpiry_doesNotThrow() {
        User user = user("u-5");
        AdminAction block = AdminAction.builder()
                .id("a-4")
                .actionType(ActionType.BLOCK)
                .expiresAt(null)
                .build();

        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-5", ActionType.BAN))
                .thenReturn(Optional.empty());
        when(adminActionRepository
                .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc("u-5", ActionType.BLOCK))
                .thenReturn(Optional.of(block));

        assertThatCode(() -> moderationPolicy.validateUserAccess(user)).doesNotThrowAnyException();
    }
}
