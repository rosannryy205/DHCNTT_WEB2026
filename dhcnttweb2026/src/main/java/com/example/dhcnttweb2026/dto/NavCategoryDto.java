package com.example.dhcnttweb2026.dto;

import java.util.List;

public record NavCategoryDto(
        Long id,
        String name,
        String slug,
        String image,
        String iconClass,
        List<NavCategoryDto> subCategories
) {
    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }
}
