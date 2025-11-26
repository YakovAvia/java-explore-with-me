package ru.practicum.main.category.service;

import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    // Admin methods
    CategoryDto createCategory(NewCategoryDto newCategoryDto);
    void deleteCategory(Long catId);
    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    // Public methods
    List<CategoryDto> getCategories(Integer from, Integer size);
    CategoryDto getCategoryById(Long catId);
}
