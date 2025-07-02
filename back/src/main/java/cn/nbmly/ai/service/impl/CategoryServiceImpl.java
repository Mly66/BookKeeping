package cn.nbmly.ai.service.impl;

import cn.nbmly.ai.dto.CategoryDTO;
import cn.nbmly.ai.dto.CategoryRequest;
import cn.nbmly.ai.entity.Category;
import cn.nbmly.ai.repository.CategoryRepository;
import cn.nbmly.ai.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDTO createCategory(CategoryRequest categoryRequest, Long userId) {
        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setType(categoryRequest.getType());
        category.setUserId(userId);
        Category savedCategory = categoryRepository.save(category);
        return toDto(savedCategory);
    }

    @Override
    public CategoryDTO getCategoryById(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该分类");
        }
        return toDto(category);
    }

    @Override
    public List<CategoryDTO> getCategoriesByUser(Long userId) {
        return categoryRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryRequest categoryRequest, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该分类");
        }
        category.setName(categoryRequest.getName());
        category.setType(categoryRequest.getType());
        Category updatedCategory = categoryRepository.save(category);
        return toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该分类");
        }
        // TODO: Consider what happens to bills when a category is deleted.
        categoryRepository.delete(category);
    }

    private CategoryDTO toDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setUserId(category.getUserId());
        return dto;
    }
}