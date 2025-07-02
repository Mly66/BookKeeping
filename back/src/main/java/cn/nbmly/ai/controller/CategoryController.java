package cn.nbmly.ai.controller;

import cn.nbmly.ai.dto.CategoryDTO;
import cn.nbmly.ai.dto.CategoryRequest;
import cn.nbmly.ai.entity.User;
import cn.nbmly.ai.service.CategoryService;
import cn.nbmly.ai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryRequest categoryRequest,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        CategoryDTO createdCategory = categoryService.createCategory(categoryRequest, currentUser.getId());
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getUserCategories(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<CategoryDTO> categories = categoryService.getCategoriesByUser(currentUser.getId());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        CategoryDTO category = categoryService.getCategoryById(id, currentUser.getId());
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryRequest, currentUser.getId());
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        categoryService.deleteCategory(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}