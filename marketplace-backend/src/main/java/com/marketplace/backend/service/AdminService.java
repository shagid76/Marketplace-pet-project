package com.marketplace.backend.service;

import com.marketplace.backend.dto.AdminActionDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.AdminActionMapper;
import com.marketplace.backend.model.Admin.*;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductDocument;
import com.marketplace.backend.model.Product.ProductStatus;
import com.marketplace.backend.model.Review.Review;
import com.marketplace.backend.model.Review.ReviewStatus;
import com.marketplace.backend.model.User.User;
import com.marketplace.backend.model.User.UserStatus;
import com.marketplace.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ReviewRepository reviewRepository;
    private final AdminActionRepository adminActionRepository;
    private final AdminActionMapper adminActionMapper;
    private final SseEmitterService sseEmitterService;
    private final UserRepository userRepository;

    public AdminActionDto findById(String id) {
        AdminAction adminAction = findEntityById(id);
        return adminActionMapper.convertToDto(adminAction, isActive(adminAction));
    }

    private AdminAction findEntityById(String id) {
        return adminActionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No Admin Action found with id: " + id));
    }

    public AdminActionDto getActive(String targetId, TargetType targetType) {
        return adminActionRepository
                .findFirstByTargetIdAndTargetTypeAndRevokedAtIsNull(targetId, targetType)
                .map(action -> adminActionMapper.convertToDto(action, isActive(action)))
                .orElseThrow(() -> new NotFoundException("No active action found"));
    }

    public AdminActionDto create(CreateAdminActionRequest createAdminActionRequest, String currentUserId) {
        log.info("Admin action created: type={}, targetType={}, targetId={}, by={}",
                createAdminActionRequest.getActionType(), createAdminActionRequest.getTargetType(),
                createAdminActionRequest.getTargetId(), currentUserId);
        AdminAction adminAction = createAdminAction(createAdminActionRequest, currentUserId);
        applyActionToTarget(adminAction.getId(), adminAction.getTargetType(), adminAction.getTargetId(), adminAction.getActionType());
        return adminActionMapper.convertToDto(adminAction, isActive(adminAction));
    }

    private AdminAction createAdminAction(CreateAdminActionRequest createAdminActionRequest, String currentUserId) {
        AdminAction adminAction = AdminAction.builder()
                .id(UUID.randomUUID().toString())
                .actorId(currentUserId)
                .actionType(createAdminActionRequest.getActionType())
                .targetType(createAdminActionRequest.getTargetType())
                .targetId(createAdminActionRequest.getTargetId())
                .reason(createAdminActionRequest.getReason())
                .createdAt(Instant.now())
                .expiresAt(createAdminActionRequest.getExpiresAt())
                .build();
        return adminActionRepository.save(adminAction);
    }

    private void applyActionToTarget(String moderationActionId, TargetType targetType, String targetId,
                                     ActionType actionType) {
        switch (targetType) {
            case PRODUCT -> applyModerationToProduct(targetId, moderationActionId);

            case REVIEW -> applyModerationToReview(targetId, moderationActionId);

            case USER -> handleUserAction(targetId, actionType);

            default -> throw new IllegalStateException(
                    "Unsupported target type: " + targetType
            );
        }
    }

    private void applyModerationToProduct(String targetId, String moderationActionId) {
        Product product = findProductById(targetId);
        if (product != null) {
            ProductDocument productDocument = findProductDocumentById(targetId);
            product.setProductStatus(ProductStatus.BANNED);
            product.setModerationActionId(moderationActionId);
            productDocument.setBanned(true);
            productRepository.save(product);
            productSearchRepository.save(productDocument);
        }
    }

    private void applyModerationToReview(String targetId, String moderationActionId) {
        Review review = findReviewById(targetId);
        review.setReviewStatus(ReviewStatus.BANNED);
        review.setModerationActionId(moderationActionId);
        reviewRepository.save(review);
    }

    private void handleUserAction(String targetId, ActionType actionType) {
        String event = switch (actionType) {
            case BAN -> "ACCOUNT_BANNED";
            case BLOCK -> "ACCOUNT_BLOCKED";
            default -> null;
        };
        if (event != null) {
            sseEmitterService.push(targetId, event);
        }
    }

    private ProductDocument findProductDocumentById(String id) {
        return productSearchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No Product Document found with id: " + id));
    }

    private Product findProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    private Review findReviewById(String id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No review found with id: " + id));
    }

    private User findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No user found with id: " + id));
    }

    private boolean isActive(AdminAction adminAction) {
        return adminAction.getRevokedAt() == null &&
                (adminAction.getExpiresAt() == null ||
                        adminAction.getExpiresAt().isAfter(Instant.now()));
    }

    public void revoke(String adminActionId) {
        AdminAction adminAction = findEntityById(adminActionId);
        deleteAdminAction(adminAction.getTargetId(), adminAction.getTargetType());
        log.info("Admin action revoked: id={}, targetType={}, targetId={}",
                adminActionId, adminAction.getTargetType(), adminAction.getTargetId());
        adminActionRepository.delete(adminAction);
    }

    private void deleteAdminAction(String targetId, TargetType targetType) {
        switch (targetType) {
            case PRODUCT -> restoreProduct(targetId);
            case REVIEW -> restoreReview(targetId);
            case USER -> restoreUser(targetId);
            default -> throw new IllegalArgumentException(
                    "Unsupported target type: " + targetType
            );
        }
    }

    private void restoreUser(String userId) {
        User user = findUserById(userId);

        user.setUserStatus(UserStatus.ACTIVE);
        user.setModerationActionId(null);

        userRepository.save(user);
    }

    private void restoreProduct(String productId) {
        Product product = findProductById(productId);
        ProductDocument productDocument = findProductDocumentById(productId);

        product.setProductStatus(ProductStatus.ACTIVE);
        product.setModerationActionId(null);

        productDocument.setBanned(false);

        productRepository.save(product);
        productSearchRepository.save(productDocument);
    }

    private void restoreReview(String reviewId) {
        Review review = findReviewById(reviewId);

        review.setReviewStatus(ReviewStatus.ACTIVE);
        review.setModerationActionId(null);

        reviewRepository.save(review);
    }

    public AdminActionDto extend(ExtendAdminActionRequest extendAdminActionRequest, String adminActionId) {
        AdminAction adminAction = findEntityById(adminActionId);

        if (adminAction.getRevokedAt() != null) {
            throw new IllegalStateException("Cannot edit revoked action");
        }

        if (extendAdminActionRequest.getNewExpiresAt() != null) {
            adminAction.setExpiresAt(extendAdminActionRequest.getNewExpiresAt());
            adminAction.setReason(extendAdminActionRequest.getReason());
        } else {
            throw new IllegalArgumentException("Either newExpiresAt or revoke must be provided");
        }

        adminActionRepository.save(adminAction);
        return adminActionMapper.convertToDto(adminAction, isActive(adminAction));
    }
}