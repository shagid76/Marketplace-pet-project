package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.PageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageMapper {
    public <T> PageResponseDto<T> mapToDto(int page, int size, Page<T> content) {
        return PageResponseDto.<T>builder()
                .content(content.getContent())
                .page(page)
                .size(size)
                .totalElements(content.getTotalElements())
                .totalPages(content.getTotalPages())
                .build();
    }
}
