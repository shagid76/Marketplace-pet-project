package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.AdminActionDto;
import com.marketplace.backend.model.Admin.AdminAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AdminActionMapper {
    public AdminActionDto convertToDto(AdminAction adminAction, boolean isActive) {
        return AdminActionDto.builder()
                .id(adminAction.getId())
                .actorId(adminAction.getActorId())
                .actionType(adminAction.getActionType())
                .targetType(adminAction.getTargetType())
                .targetId(adminAction.getTargetId())
                .reason(adminAction.getReason())
                .createdAt(adminAction.getCreatedAt())
                .expiresAt(adminAction.getExpiresAt())
                .revokedAt(adminAction.getRevokedAt())
                .revokedBy(adminAction.getRevokedBy())
                .revokeReason(adminAction.getRevokeReason())
                .active(isActive)
                .build();
    }
}