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
import com.marketplace.backend.repository.AdminActionRepository;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.ProductSearchRepository;
import com.marketplace.backend.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

	@Mock ProductRepository productRepository;
	@Mock ProductSearchRepository productSearchRepository;
	@Mock ReviewRepository reviewRepository;
	@Mock AdminActionRepository adminActionRepository;
	@Mock AdminActionMapper adminActionMapper;
	@Mock SseEmitterService sseEmitterService;

	@InjectMocks AdminService adminService;

	@Test
	void create_productTarget_marksProductBanned() {
		Product product = Product.builder()
				.id("p-1").productStatus(ProductStatus.ACTIVE).inStock(true).build();
		ProductDocument doc = ProductDocument.builder().id("p-1").banned(false).build();

		when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
		when(productSearchRepository.findById("p-1")).thenReturn(Optional.of(doc));
		when(adminActionRepository.save(any(AdminAction.class))).thenAnswer(inv -> inv.getArgument(0));
		when(adminActionMapper.convertToDto(any(AdminAction.class), anyBoolean()))
				.thenReturn(new AdminActionDto());

		CreateAdminActionRequest req = CreateAdminActionRequest.builder()
				.targetId("p-1")
				.targetType(TargetType.PRODUCT)
				.actionType(ActionType.BAN)
				.reason("scam")
				.build();

		adminService.create(req, "moderator-1");

		ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
		verify(productRepository).save(captor.capture());
		assertThat(captor.getValue().getProductStatus()).isEqualTo(ProductStatus.BANNED);
		assertThat(captor.getValue().getModerationActionId()).isNotNull();

		ArgumentCaptor<ProductDocument> docCaptor = ArgumentCaptor.forClass(ProductDocument.class);
		verify(productSearchRepository).save(docCaptor.capture());
		assertThat(docCaptor.getValue().isBanned()).isTrue();
	}

	@Test
	void create_reviewTarget_marksReviewBanned() {
		Review review = Review.builder()
				.id("r-1").reviewStatus(ReviewStatus.ACTIVE).build();

		when(reviewRepository.findById("r-1")).thenReturn(Optional.of(review));
		when(adminActionRepository.save(any(AdminAction.class))).thenAnswer(inv -> inv.getArgument(0));
		when(adminActionMapper.convertToDto(any(AdminAction.class), anyBoolean()))
				.thenReturn(new AdminActionDto());

		CreateAdminActionRequest req = CreateAdminActionRequest.builder()
				.targetId("r-1")
				.targetType(TargetType.REVIEW)
				.actionType(ActionType.BAN)
				.build();

		adminService.create(req, "moderator-1");

		assertThat(review.getReviewStatus()).isEqualTo(ReviewStatus.BANNED);
		assertThat(review.getModerationActionId()).isNotNull();
		verify(reviewRepository).save(review);
	}

	@Test
	void create_userBan_pushesSseEvent() {
		when(adminActionRepository.save(any(AdminAction.class))).thenAnswer(inv -> inv.getArgument(0));
		when(adminActionMapper.convertToDto(any(AdminAction.class), anyBoolean()))
				.thenReturn(new AdminActionDto());

		CreateAdminActionRequest req = CreateAdminActionRequest.builder()
				.targetId("user-1")
				.targetType(TargetType.USER)
				.actionType(ActionType.BAN)
				.build();

		adminService.create(req, "moderator-1");

		verify(sseEmitterService).push("user-1", "ACCOUNT_BANNED");
	}

	@Test
	void create_userBlock_pushesBlockEvent() {
		when(adminActionRepository.save(any(AdminAction.class))).thenAnswer(inv -> inv.getArgument(0));
		when(adminActionMapper.convertToDto(any(AdminAction.class), anyBoolean()))
				.thenReturn(new AdminActionDto());

		CreateAdminActionRequest req = CreateAdminActionRequest.builder()
				.targetId("user-1")
				.targetType(TargetType.USER)
				.actionType(ActionType.BLOCK)
				.build();

		adminService.create(req, "moderator-1");

		verify(sseEmitterService).push("user-1", "ACCOUNT_BLOCKED");
	}

	@Test
	void revoke_productAction_restoresProduct() {
		AdminAction action = AdminAction.builder()
				.id("a-1").targetId("p-1").targetType(TargetType.PRODUCT).build();
		Product banned = Product.builder()
				.id("p-1").productStatus(ProductStatus.BANNED).moderationActionId("a-1").build();
		ProductDocument doc = ProductDocument.builder().id("p-1").banned(true).build();

		when(adminActionRepository.findById("a-1")).thenReturn(Optional.of(action));
		when(productRepository.findById("p-1")).thenReturn(Optional.of(banned));
		when(productSearchRepository.findById("p-1")).thenReturn(Optional.of(doc));

		adminService.revoke("a-1");

		assertThat(banned.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
		assertThat(banned.getModerationActionId()).isNull();
		assertThat(doc.isBanned()).isFalse();
		verify(adminActionRepository).delete(action);
	}

	@Test
	void revoke_reviewAction_restoresReview() {
		AdminAction action = AdminAction.builder()
				.id("a-1").targetId("r-1").targetType(TargetType.REVIEW).build();
		Review banned = Review.builder()
				.id("r-1").reviewStatus(ReviewStatus.BANNED).moderationActionId("a-1").build();

		when(adminActionRepository.findById("a-1")).thenReturn(Optional.of(action));
		when(reviewRepository.findById("r-1")).thenReturn(Optional.of(banned));

		adminService.revoke("a-1");

		assertThat(banned.getReviewStatus()).isEqualTo(ReviewStatus.ACTIVE);
		assertThat(banned.getModerationActionId()).isNull();
	}

	@Test
	void findById_missing_throwsNotFound() {
		when(adminActionRepository.findById("ghost")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> adminService.findById("ghost"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void extend_revokedAction_throwsIllegalState() {
		AdminAction revoked = AdminAction.builder()
				.id("a-1").revokedAt(java.time.Instant.now()).build();
		when(adminActionRepository.findById("a-1")).thenReturn(Optional.of(revoked));

		ExtendAdminActionRequest req = new ExtendAdminActionRequest();
		req.setNewExpiresAt(java.time.Instant.now().plusSeconds(3600));

		assertThatThrownBy(() -> adminService.extend(req, "a-1"))
				.isInstanceOf(IllegalStateException.class);
	}
}
