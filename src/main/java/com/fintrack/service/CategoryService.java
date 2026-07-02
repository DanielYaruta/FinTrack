package com.fintrack.service;

import com.fintrack.dto.CategoryDto;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Category;
import com.fintrack.model.TransactionType;
import com.fintrack.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findByType(TransactionType type) {
        return categoryRepository.findByType(type);
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Категория", id));
    }

    @Transactional
    public Category save(CategoryDto dto) {
        Category category = new Category(dto.getName(), dto.getType());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, CategoryDto dto) {
        Category category = findById(id);
        category.setName(dto.getName());
        category.setType(dto.getType());
        return category;
    }

    @Transactional
    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Категория", id);
        }
        categoryRepository.deleteById(id);
    }
}
