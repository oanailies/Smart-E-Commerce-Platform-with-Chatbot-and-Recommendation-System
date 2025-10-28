package product_service.application.service;

import product_service.domain.model.Category;
import product_service.domain.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Category createCategory(String name, Long parentCategoryId) {
        Category parentCategory = (parentCategoryId != null)
                ? categoryRepository.findById(parentCategoryId).orElse(null)
                : null;

        Category category = new Category(name, parentCategory);
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public List<Category> getSubcategories(Long parentId) {
        Category parent = categoryRepository.findById(parentId).orElse(null);
        if (parent == null) {
            return List.of();
        }
        return parent.getSubCategories();
    }


    public List<Long> getAllDescendantCategoryIds(Long categoryId) {
        Category rootCat = categoryRepository.findById(categoryId).orElse(null);
        if (rootCat == null) {
            return List.of();
        }

        List<Long> result = new ArrayList<>();
        gatherIds(rootCat, result);
        return result;
    }

    private void gatherIds(Category category, List<Long> collector) {
        collector.add(category.getId());
        if (category.getSubCategories() != null) {
            for (Category sub : category.getSubCategories()) {
                gatherIds(sub, collector);
            }
        }
    }


    public List<Category> getParentChain(Long categoryId) {
        List<Category> chain = new ArrayList<>();
        Category current = categoryRepository.findById(categoryId).orElse(null);
        if (current == null) {
            return chain;
        }
        while (current != null) {
            chain.add(0, current);
            current = current.getParentCategory();
        }
        return chain;
    }

    public boolean deleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }






}
