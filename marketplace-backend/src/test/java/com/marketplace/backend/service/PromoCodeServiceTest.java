package com.marketplace.backend.service;

import com.marketplace.backend.dto.PromoCodeDto;
import com.marketplace.backend.exception.BadRequestException;
import com.marketplace.backend.exception.PromoCodeException;
import com.marketplace.backend.mapper.PageMapper;
import com.marketplace.backend.mapper.PromoCodeMapper;
import com.marketplace.backend.model.Product.Category;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.PromoCode.CheckPromoCodeRequest;
import com.marketplace.backend.model.PromoCode.PromoCode;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.PromoCodeRepository;
import com.marketplace.backend.repository.PromoCodeUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

	@Mock PromoCodeRepository promoCodeRepository;
	@Mock PromoCodeUsageRepository promoCodeUsageRepository;
	@Mock ProductRepository productRepository;
	@Mock PromoCodeMapper promoCodeMapper;
	@Mock PageMapper pageMapper;

	@InjectMocks PromoCodeService promoCodeService;

	@Test
	void create_existingCode_throwsBadRequest() {
		var req = new com.marketplace.backend.model.PromoCode.CreatePromoCodeRequest();
		req.setCode("EXISTING");
		when(promoCodeRepository.existsByCode("EXISTING")).thenReturn(true);

		assertThatThrownBy(() -> promoCodeService.create(req))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	void checkPromoCode_inactivePromo_rejects() {
		CheckPromoCodeRequest req = new CheckPromoCodeRequest();
		req.setCode("OLD");
		req.setProductIds(List.of("p-1"));

		when(productRepository.findAllById(List.of("p-1")))
				.thenReturn(List.of(activeProduct("p-1", Category.ELECTRONICS)));
		when(promoCodeRepository.findByCode("OLD"))
				.thenReturn(Optional.of(PromoCode.builder().code("OLD").active(false).build()));

		assertThatThrownBy(() -> promoCodeService.checkPromoCode(req, "user-1"))
				.isInstanceOf(PromoCodeException.class)
				.hasMessageContaining("not active");
	}

	@Test
	void checkPromoCode_expired_rejects() {
		CheckPromoCodeRequest req = new CheckPromoCodeRequest();
		req.setCode("EXP");
		req.setProductIds(List.of("p-1"));

		when(productRepository.findAllById(List.of("p-1")))
				.thenReturn(List.of(activeProduct("p-1", Category.ELECTRONICS)));
		when(promoCodeRepository.findByCode("EXP"))
				.thenReturn(Optional.of(PromoCode.builder()
						.code("EXP").active(true)
						.startAt(LocalDateTime.now().minusDays(10))
						.endAt(LocalDateTime.now().minusDays(1))
						.applicableCategories(List.of(Category.ELECTRONICS))
						.requiredProducts(1)
						.requiredPrice(BigDecimal.ZERO)
						.maxUsagePerUser(10)
						.build()));

		assertThatThrownBy(() -> promoCodeService.checkPromoCode(req, "user-1"))
				.isInstanceOf(PromoCodeException.class)
				.hasMessageContaining("expired");
	}

	@Test
	void checkPromoCode_notStarted_rejects() {
		CheckPromoCodeRequest req = new CheckPromoCodeRequest();
		req.setCode("FUTURE");
		req.setProductIds(List.of("p-1"));

		when(productRepository.findAllById(List.of("p-1")))
				.thenReturn(List.of(activeProduct("p-1", Category.ELECTRONICS)));
		when(promoCodeRepository.findByCode("FUTURE"))
				.thenReturn(Optional.of(PromoCode.builder()
						.code("FUTURE").active(true)
						.startAt(LocalDateTime.now().plusDays(1))
						.endAt(LocalDateTime.now().plusDays(10))
						.applicableCategories(List.of(Category.ELECTRONICS))
						.requiredProducts(1)
						.requiredPrice(BigDecimal.ZERO)
						.maxUsagePerUser(10)
						.build()));

		assertThatThrownBy(() -> promoCodeService.checkPromoCode(req, "user-1"))
				.isInstanceOf(PromoCodeException.class)
				.hasMessageContaining("not started");
	}

	@Test
	void checkPromoCode_wrongCategory_rejects() {
		CheckPromoCodeRequest req = new CheckPromoCodeRequest();
		req.setCode("ELEC");
		req.setProductIds(List.of("p-1"));

		when(productRepository.findAllById(List.of("p-1")))
				.thenReturn(List.of(activeProduct("p-1", Category.FASHION))); // wrong cat
		when(promoCodeRepository.findByCode("ELEC"))
				.thenReturn(Optional.of(PromoCode.builder()
						.code("ELEC").active(true)
						.startAt(LocalDateTime.now().minusDays(1))
						.endAt(LocalDateTime.now().plusDays(1))
						.applicableCategories(List.of(Category.ELECTRONICS))
						.requiredProducts(1)
						.requiredPrice(BigDecimal.ZERO)
						.maxUsagePerUser(10)
						.build()));

		assertThatThrownBy(() -> promoCodeService.checkPromoCode(req, "user-1"))
				.isInstanceOf(PromoCodeException.class)
				.hasMessageContaining("not applicable");
	}

	@Test
	void checkPromoCode_minTotalNotMet_rejects() {
		CheckPromoCodeRequest req = new CheckPromoCodeRequest();
		req.setCode("BIG");
		req.setProductIds(List.of("p-1"));

		Product cheap = activeProduct("p-1", Category.ELECTRONICS);
		cheap.setPrice(BigDecimal.valueOf(10));

		when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of(cheap));
		when(promoCodeRepository.findByCode("BIG"))
				.thenReturn(Optional.of(PromoCode.builder()
						.code("BIG").active(true)
						.startAt(LocalDateTime.now().minusDays(1))
						.endAt(LocalDateTime.now().plusDays(1))
						.applicableCategories(List.of(Category.ELECTRONICS))
						.requiredProducts(1)
						.requiredPrice(BigDecimal.valueOf(1000))
						.maxUsagePerUser(10)
						.build()));

		assertThatThrownBy(() -> promoCodeService.checkPromoCode(req, "user-1"))
				.isInstanceOf(PromoCodeException.class)
				.hasMessageContaining("required price is");
	}

	@Test
	void checkPromoCode_usageLimitExceeded_rejects() {
		CheckPromoCodeRequest req = new CheckPromoCodeRequest();
		req.setCode("ONCE");
		req.setProductIds(List.of("p-1"));

		Product product = activeProduct("p-1", Category.ELECTRONICS);
		product.setPrice(BigDecimal.valueOf(100));

		when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of(product));
		when(promoCodeRepository.findByCode("ONCE"))
				.thenReturn(Optional.of(PromoCode.builder()
						.id("promo-1").code("ONCE").active(true)
						.startAt(LocalDateTime.now().minusDays(1))
						.endAt(LocalDateTime.now().plusDays(1))
						.applicableCategories(List.of(Category.ELECTRONICS))
						.requiredProducts(1)
						.requiredPrice(BigDecimal.ZERO)
						.maxUsagePerUser(1)
						.build()));
		when(promoCodeUsageRepository.countByUserIdAndPromoCodeId("user-1", "promo-1"))
				.thenReturn(1);

		assertThatThrownBy(() -> promoCodeService.checkPromoCode(req, "user-1"))
				.isInstanceOf(PromoCodeException.class)
				.hasMessageContaining("used count");
	}

	@Test
	void checkPromoCode_happyPath_returnsDto() {
		CheckPromoCodeRequest req = new CheckPromoCodeRequest();
		req.setCode("OK");
		req.setProductIds(List.of("p-1"));

		Product product = activeProduct("p-1", Category.ELECTRONICS);
		product.setPrice(BigDecimal.valueOf(100));

		PromoCode promo = PromoCode.builder()
				.id("promo-1").code("OK").active(true)
				.startAt(LocalDateTime.now().minusDays(1))
				.endAt(LocalDateTime.now().plusDays(1))
				.applicableCategories(List.of(Category.ELECTRONICS))
				.requiredProducts(1)
				.requiredPrice(BigDecimal.ZERO)
				.maxUsagePerUser(10)
				.build();

		when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of(product));
		when(promoCodeRepository.findByCode("OK")).thenReturn(Optional.of(promo));
		when(promoCodeUsageRepository.countByUserIdAndPromoCodeId("user-1", "promo-1"))
				.thenReturn(0);
		when(promoCodeMapper.mapToDto(promo)).thenReturn(new PromoCodeDto());

		PromoCodeDto result = promoCodeService.checkPromoCode(req, "user-1");

		assertThat(result).isNotNull();
	}

	@Test
	void checkPromoCode_noProducts_rejects() {
		CheckPromoCodeRequest req = new CheckPromoCodeRequest();
		req.setCode("ANY");
		req.setProductIds(List.of("p-1"));
		when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of());

		assertThatThrownBy(() -> promoCodeService.checkPromoCode(req, "user-1"))
				.isInstanceOf(PromoCodeException.class)
				.hasMessageContaining("No products");
	}

	private Product activeProduct(String id, Category category) {
		return Product.builder()
				.id(id)
				.price(BigDecimal.ZERO)
				.category(category)
				.build();
	}
}
