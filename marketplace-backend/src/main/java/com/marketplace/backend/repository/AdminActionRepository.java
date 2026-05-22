package com.marketplace.backend.repository;

import com.marketplace.backend.model.Admin.ActionType;
import com.marketplace.backend.model.Admin.AdminAction;
import com.marketplace.backend.model.Admin.TargetType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminActionRepository extends MongoRepository<AdminAction, String> {

    Optional<AdminAction> findFirstByTargetIdAndTargetTypeAndRevokedAtIsNull(
            String targetId,
            TargetType targetType
    );

    Optional<AdminAction> findTopByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc(
            String targetId, ActionType actionType);

    Optional<AdminAction> findFirstByTargetIdAndActionTypeAndRevokedAtIsNullOrderByCreatedAtDesc(
            String targetId, ActionType actionType);

    boolean existsByTargetId(String targetId);
}