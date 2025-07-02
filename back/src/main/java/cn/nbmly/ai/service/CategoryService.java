package cn.nbmly.ai.service;

import cn.nbmly.ai.dto.CategoryDTO;
import cn.nbmly.ai.dto.CategoryRequest;
import cn.nbmly.ai.entity.Category;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryRequest categoryRequest, Long userId);

    CategoryDTO getCategoryById(Long id, Long userId);

    List<CategoryDTO> getCategoriesByUser(Long userId);

    CategoryDTO updateCategory(Long id, CategoryRequest categoryRequest, Long userId);

    void deleteCategory(Long id, Long userId);
}