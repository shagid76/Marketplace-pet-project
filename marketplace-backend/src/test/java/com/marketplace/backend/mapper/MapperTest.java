package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.*;
import com.marketplace.backend.model.Admin.ActionType;
import com.marketplace.backend.model.Admin.AdminAction;
import com.marketplace.backend.model.Admin.TargetType;
import com.marketplace.backend.model.Cart.Cart;
import com.marketplace.backend.model.Chat.Chat;
import com.marketplace.backend.model.Message.Message;
import com.marketplace.backend.model.Product.Category;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductDocument;
import com.marketplace.backend.model.Product.ProductStatus;
import com.marketplace.backend.model.PromoCode.PromoCode;
import com.marketplace.backend.model.PromoCode.PromoCodeType;
import com.marketplace.backend.model.Report.Report;
import com.marketplace.backend.model.Report.Status;
import com.marketplace.backend.model.Review.Review;
import com.marketplace.backend.model.Review.ReviewStatus;
import com.marketplace.backend.model.User.Role;
import com.marketplace.backend.model.User.User;
import com.marketplace.backend.model.User.UserStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for all mapper classes — no Spring context needed.
 */
class MapperTest {

    // ── ProductMapper ──────────────────────────────────────────────────────────

    private final ProductMapper productMapper = new ProductMapper();

    @Test
    void productMapper_mapsAllFields() {
        Product p = Product.builder()
                .id("p-1").title("Widget").description("desc")
                .price(BigDecimal.valueOf(19.99))
                .category(Category.ELECTRONICS)
                .inStock(true).author("seller-1").buyerId("buyer-1")
                .productStatus(ProductStatus.ACTIVE)
                .moderationActionId("mod-1")
                .build();

        ProductDto dto = productMapper.mapToDto(p, List.of("http://img/1.jpg"));

        assertThat(dto.getId()).isEqualTo("p-1");
        assertThat(dto.getTitle()).isEqualTo("Widget");
        assertThat(dto.getDescription()).isEqualTo("desc");
        assertThat(dto.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(dto.getImages()).containsExactly("http://img/1.jpg");
        assertThat(dto.getCategory()).isEqualTo(Category.ELECTRONICS);
        assertThat(dto.isInStock()).isTrue();
        assertThat(dto.getAuthor()).isEqualTo("seller-1");
        assertThat(dto.getBuyerId()).isEqualTo("buyer-1");
        assertThat(dto.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(dto.getModerationActionId()).isEqualTo("mod-1");
    }

    // ── UserMapper ─────────────────────────────────────────────────────────────

    private final UserMapper userMapper = new UserMapper();

    @Test
    void userMapper_mapToDto_mapsAllFields() {
        User u = User.builder()
                .id("u-1").username("john")
                .roles(Set.of(Role.ROLE_USER))
                .userStatus(UserStatus.ACTIVE)
                .build();

        UserDto dto = userMapper.mapToDto(u, "http://cdn/avatar.jpg");

        assertThat(dto.getId()).isEqualTo("u-1");
        assertThat(dto.getUsername()).isEqualTo("john");
        assertThat(dto.getAvatar()).isEqualTo("http://cdn/avatar.jpg");
        assertThat(dto.getRole()).contains(Role.ROLE_USER);
        assertThat(dto.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void userMapper_mapToDto_nullUser_returnsNull() {
        assertThat(userMapper.mapToDto(null, "url")).isNull();
    }

    @Test
    void userMapper_mapMeToDto_includesWishlist() {
        User u = User.builder().id("u-1").username("jane").build();
        ProductDto product = ProductDto.builder().id("p-1").build();

        UserDto dto = userMapper.mapMeToDto(u, null, List.of(product));

        assertThat(dto.getWishlist()).hasSize(1);
        assertThat(dto.getWishlist().get(0).getId()).isEqualTo("p-1");
    }

    @Test
    void userMapper_mapMeToDto_nullUser_returnsNull() {
        assertThat(userMapper.mapMeToDto(null, null, List.of())).isNull();
    }

    // ── CartMapper ─────────────────────────────────────────────────────────────

    private final CartMapper cartMapper = new CartMapper();

    @Test
    void cartMapper_mapsAllFields() {
        Cart cart = Cart.builder().id("cart-1").userId("user-1").products(List.of("p-1")).build();
        ProductDto productDto = ProductDto.builder().id("p-1").build();

        CartDto dto = cartMapper.mapToDto(cart, List.of(productDto));

        assertThat(dto.getId()).isEqualTo("cart-1");
        assertThat(dto.getUserid()).isEqualTo("user-1");
        assertThat(dto.getProducts()).hasSize(1);
        assertThat(dto.getProducts().get(0).getId()).isEqualTo("p-1");
    }

    // ── ChatMapper ─────────────────────────────────────────────────────────────

    private final ChatMapper chatMapper = new ChatMapper();

    @Test
    void chatMapper_mapToDto_noMessages_mapsFields() {
        Chat chat = Chat.builder()
                .id("c-1").user1Id("alice").user2Id("bob")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        ChatDto dto = chatMapper.mapToDto(chat);

        assertThat(dto.getId()).isEqualTo("c-1");
        assertThat(dto.getUser1Id()).isEqualTo("alice");
        assertThat(dto.getUser2Id()).isEqualTo("bob");
        assertThat(dto.getMessages()).isNull();
    }

    @Test
    void chatMapper_mapToDto_nullChat_returnsNull() {
        assertThat(chatMapper.mapToDto((Chat) null)).isNull();
    }

    @Test
    void chatMapper_mapToDto_withMessages_sortsByCreatedAt() {
        Chat chat = Chat.builder().id("c-1").user1Id("alice").user2Id("bob").build();
        MessageDto m1 = MessageDto.builder().id("m-1").createdAt(LocalDateTime.of(2024, 1, 1, 10, 0)).build();
        MessageDto m2 = MessageDto.builder().id("m-2").createdAt(LocalDateTime.of(2024, 1, 1, 9, 0)).build();

        ChatDto dto = chatMapper.mapToDto(chat, List.of(m1, m2));

        assertThat(dto.getMessages()).extracting(MessageDto::getId)
                .containsExactly("m-2", "m-1");
    }

    @Test
    void chatMapper_mapToDto_nullMessages_treatedAsEmpty() {
        Chat chat = Chat.builder().id("c-1").user1Id("a").user2Id("b").build();
        ChatDto dto = chatMapper.mapToDto(chat, null);
        assertThat(dto.getMessages()).isEmpty();
    }

    // ── MessageMapper ──────────────────────────────────────────────────────────

    private final MessageMapper messageMapper = new MessageMapper();

    @Test
    void messageMapper_mapsAllFields_setDeletedTrue() {
        Message msg = Message.builder()
                .id("m-1").chatId("c-1").senderId("user-1").text("hello")
                .createdAt(LocalDateTime.of(2024, 6, 1, 12, 0))
                .build();

        MessageDto dto = messageMapper.mapToDto(msg, true);

        assertThat(dto.getId()).isEqualTo("m-1");
        assertThat(dto.getChatId()).isEqualTo("c-1");
        assertThat(dto.getSenderId()).isEqualTo("user-1");
        assertThat(dto.getText()).isEqualTo("hello");
        assertThat(dto.isDeleted()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 6, 1, 12, 0));
    }

    @Test
    void messageMapper_setDeletedFalse_deletedIsFalse() {
        Message msg = Message.builder().id("m-2").chatId("c-1").senderId("u").text("hi").build();
        MessageDto dto = messageMapper.mapToDto(msg, false);
        assertThat(dto.isDeleted()).isFalse();
    }

    @Test
    void messageMapper_nullMessage_returnsNull() {
        assertThat(messageMapper.mapToDto(null, false)).isNull();
    }

    // ── ReviewMapper ───────────────────────────────────────────────────────────

    private final ReviewMapper reviewMapper = new ReviewMapper();

    @Test
    void reviewMapper_mapsAllFields() {
        Review review = Review.builder()
                .id("r-1").description("great seller").rating(5.0)
                .author("buyer-1").targetId("seller-1")
                .reviewStatus(ReviewStatus.ACTIVE)
                .moderationActionId("mod-1")
                .lastUpdated(LocalDateTime.of(2024, 3, 15, 8, 0))
                .build();

        ReviewDto dto = reviewMapper.mapToDto(review);

        assertThat(dto.getId()).isEqualTo("r-1");
        assertThat(dto.getDescription()).isEqualTo("great seller");
        assertThat(dto.getRating()).isEqualTo(5.0);
        assertThat(dto.getAuthor()).isEqualTo("buyer-1");
        assertThat(dto.getTargetId()).isEqualTo("seller-1");
        assertThat(dto.getReviewStatus()).isEqualTo(ReviewStatus.ACTIVE);
        assertThat(dto.getModerationActionId()).isEqualTo("mod-1");
    }

    // ── ReportMapper ───────────────────────────────────────────────────────────

    private final ReportMapper reportMapper = new ReportMapper();

    @Test
    void reportMapper_mapsAllFields() {
        Report report = Report.builder()
                .id("rep-1").authorId("user-1").targetId("target-1")
                .description("spam account")
                .status(Status.ACTIVE)
                .targetType(com.marketplace.backend.model.Report.TargetType.USER)
                .build();

        ReportDto dto = reportMapper.mapToDto(report);

        assertThat(dto.getId()).isEqualTo("rep-1");
        assertThat(dto.getAuthorId()).isEqualTo("user-1");
        assertThat(dto.getTargetId()).isEqualTo("target-1");
        assertThat(dto.getDescription()).isEqualTo("spam account");
        assertThat(dto.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(dto.getTargetType()).isEqualTo(com.marketplace.backend.model.Report.TargetType.USER);
    }


    private final PromoCodeMapper promoCodeMapper = new PromoCodeMapper();

    @Test
    void promoCodeMapper_mapsAllFields() {
        PromoCode promo = PromoCode.builder()
                .id("promo-1").code("SAVE10")
                .stripeCouponId("coupon-abc")
                .promoCodeType(PromoCodeType.PERCENTAGE)
                .discountValue(10.0)
                .maxUsagePerUser(3)
                .requiredProducts(1)
                .requiredPrice(BigDecimal.valueOf(50))
                .applicableCategories(List.of(Category.ELECTRONICS))
                .active(true)
                .startAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .endAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        PromoCodeDto dto = promoCodeMapper.mapToDto(promo);

        assertThat(dto.getId()).isEqualTo("promo-1");
        assertThat(dto.getCode()).isEqualTo("SAVE10");
        assertThat(dto.getStripeCouponId()).isEqualTo("coupon-abc");
        assertThat(dto.getPromoCodeType()).isEqualTo(PromoCodeType.PERCENTAGE);
        assertThat(dto.getDiscountValue()).isEqualTo(10.0);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getApplicableCategories()).containsExactly(Category.ELECTRONICS);
    }

    // ── ProductDocumentMapper ──────────────────────────────────────────────────

    private final ProductDocumentMapper productDocumentMapper = new ProductDocumentMapper();

    @Test
    void productDocumentMapper_mapDocumentToDto_mapsAllFields() {
        ProductDocument doc = ProductDocument.builder()
                .id("p-1").title("Widget").description("good one")
                .price(BigDecimal.TEN).category(Category.ELECTRONICS)
                .inStock(true).locked(false).banned(false)
                .author("seller-1").images(List.of("img1.jpg"))
                .build();

        ProductDocumentDto dto = productDocumentMapper.mapDocumentToDto(doc);

        assertThat(dto.getId()).isEqualTo("p-1");
        assertThat(dto.getTitle()).isEqualTo("Widget");
        assertThat(dto.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(dto.isInStock()).isTrue();
        assertThat(dto.isLocked()).isFalse();
        assertThat(dto.isBanned()).isFalse();
        assertThat(dto.getImages()).containsExactly("img1.jpg");
        assertThat(dto.getAuthor()).isEqualTo("seller-1");
    }

    @Test
    void productDocumentMapper_mapToDocument_mapsFromProduct() {
        Product p = Product.builder()
                .id("p-1").title("Widget").description("d")
                .price(BigDecimal.TEN).category(Category.FASHION)
                .inStock(true).author("seller-1")
                .build();

        ProductDocument doc = productDocumentMapper.mapToDocument(p, List.of("http://img/1.jpg"));

        assertThat(doc.getId()).isEqualTo("p-1");
        assertThat(doc.getTitle()).isEqualTo("Widget");
        assertThat(doc.isLocked()).isFalse();
        assertThat(doc.isBanned()).isFalse();
        assertThat(doc.getImages()).containsExactly("http://img/1.jpg");
        assertThat(doc.getCategory()).isEqualTo(Category.FASHION);
    }

    // ── AdminActionMapper ──────────────────────────────────────────────────────

    private final AdminActionMapper adminActionMapper = new AdminActionMapper();

    @Test
    void adminActionMapper_mapsAllFields_activeTrue() {
        AdminAction action = AdminAction.builder()
                .id("act-1").actorId("mod-1").targetId("user-1")
                .actionType(ActionType.BAN).targetType(TargetType.USER)
                .reason("spam").createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .expiresAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();

        AdminActionDto dto = adminActionMapper.convertToDto(action, true);

        assertThat(dto.getId()).isEqualTo("act-1");
        assertThat(dto.getActorId()).isEqualTo("mod-1");
        assertThat(dto.getTargetId()).isEqualTo("user-1");
        assertThat(dto.getActionType()).isEqualTo(ActionType.BAN);
        assertThat(dto.getTargetType()).isEqualTo(TargetType.USER);
        assertThat(dto.getReason()).isEqualTo("spam");
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    void adminActionMapper_activeFalse_setsActiveFalse() {
        AdminAction action = AdminAction.builder()
                .id("act-2").actorId("mod-1").targetId("user-2")
                .actionType(ActionType.BLOCK).targetType(TargetType.USER)
                .build();

        AdminActionDto dto = adminActionMapper.convertToDto(action, false);

        assertThat(dto.isActive()).isFalse();
    }
}
