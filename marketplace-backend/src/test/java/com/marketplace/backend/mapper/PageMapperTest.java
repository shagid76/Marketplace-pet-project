package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.PageResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageMapperTest {

	private final PageMapper mapper = new PageMapper();

	@Test
	void mapToDto_preservesAllFields() {
		List<String> content = List.of("a", "b", "c");
		Page<String> page = new PageImpl<>(content, PageRequest.of(0, 3), 27);

		PageResponseDto<String> dto = mapper.mapToDto(0, 3, page);

		assertThat(dto.getContent()).containsExactly("a", "b", "c");
		assertThat(dto.getPage()).isEqualTo(0);
		assertThat(dto.getSize()).isEqualTo(3);
		assertThat(dto.getTotalElements()).isEqualTo(27);
		assertThat(dto.getTotalPages()).isEqualTo(9);
	}

	@Test
	void mapToDto_emptyPage() {
		Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

		PageResponseDto<String> dto = mapper.mapToDto(0, 10, page);

		assertThat(dto.getContent()).isEmpty();
		assertThat(dto.getTotalElements()).isZero();
		assertThat(dto.getTotalPages()).isZero();
	}

	@Test
	void mapToDto_isGenericOverDifferentTypes() {
		// proves the generic refactor — used to require unsafe casts
		Page<Integer> intPage = new PageImpl<>(List.of(1, 2, 3));
		PageResponseDto<Integer> dto = mapper.mapToDto(0, 3, intPage);
		assertThat(dto.getContent()).containsExactly(1, 2, 3);
	}
}
