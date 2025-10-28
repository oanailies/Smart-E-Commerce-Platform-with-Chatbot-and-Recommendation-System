package product_service.application.controller;

import product_service.application.service.CategoryService;
import product_service.domain.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Map<String, Object> requestBody) {
        try {
            String name = (String) requestBody.get("name");
            Long parentCategoryId = null;
            Object parentCategoryObj = requestBody.get("parentCategory");
            if (parentCategoryObj instanceof Map<?, ?> parentCategoryMap) {
                Object idObj = parentCategoryMap.get("id");
                if (idObj instanceof Number idNumber) {
                    parentCategoryId = idNumber.longValue();
                }
            }

            Category category = categoryService.createCategory(name, parentCategoryId);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }


    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return (category != null) ? ResponseEntity.ok(category) : ResponseEntity.notFound().build();
    }


    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable Long id) {
        List<Category> subs = categoryService.getSubcategories(id);
        return ResponseEntity.ok(subs);
    }

    @GetMapping("/{id}/chain")
    public ResponseEntity<List<Category>> getCategoryChain(@PathVariable Long id) {
        List<Category> chain = categoryService.getParentChain(id);
        return ResponseEntity.ok(chain);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        boolean deleted = categoryService.deleteCategory(id);
        if (deleted) {
            return ResponseEntity.ok("Category deleted successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}
