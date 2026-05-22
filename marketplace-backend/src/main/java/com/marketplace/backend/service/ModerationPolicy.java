package com.marketplace.backend.service;

import com.marketplace.backend.exception.AuthenticationException;
import com.marketplace.backend.model.Admin.ActionType;
import com.marketplace.backend.model.Admin.AdminAction;
import com.marketplace.backend.model.User.User;
import com.marketplace.backend.repository.AdminActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModerationPolicy {

    private final AdminActionRepository adminActionRepository;

    public void validateUserAccess(User user) {

        checkBan(user);
        checkBlock(user);
    }

    private void checkBan(User user) {
        Optional<AdminAction> banAction =
                adminActionRepository
                        .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc(
                                user.getId(),
                                ActionType.BAN
                        );

        if (banAction.isPresent()) {
            throw new AuthenticationException(
                    "Your account is permanently banned"
            );
        }
    }

    private void checkBlock(User user) {

        Optional<AdminAction> blockAction =
                adminActionRepository
                        .findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc(
                                user.getId(),
                                ActionType.BLOCK
                        );

        if (blockAction.isPresent()
                && blockAction.get().getExpiresAt() != null
                && blockAction.get().getExpiresAt().isAfter(Instant.now())) {

            String formattedDate =
                    blockAction.get()
                            .getExpiresAt()
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));

            throw new AuthenticationException(
                    "Your account is temporarily blocked until "
                            + formattedDate
            );
        }
    }
}
