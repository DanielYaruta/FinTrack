package com.fintrack.service;

import com.fintrack.dto.CategoryDto;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Category;
import com.fintrack.model.TransactionType;
import com.fintrack.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    // --- findAll ---

    @Test
    void findAll_shouldReturnAllCategories() {
        List<Category> expected = List.of(
                new Category("Зарплата", TransactionType.INCOME),
                new Category("Продукты", TransactionType.EXPENSE)
        );
        given(categoryRepository.findAll()).willReturn(expected);

        List<Category> result = categoryService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).isSameAs(expected);
    }

    @Test
    void findByType_shouldFilterByTransactionType() {
        List<Category> incomeCategories = List.of(new Category("Зарплата", TransactionType.INCOME));
        given(categoryRepository.findByType(TransactionType.INCOME)).willReturn(incomeCategories);

        List<Category> result = categoryService.findByType(TransactionType.INCOME);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(TransactionType.INCOME);
    }

    // --- findById ---

    @Test
    void findById_whenNotExists_shouldThrowResourceNotFoundException() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- save ---

    @Test
    void save_shouldCreateCategoryFromDto() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Спорт");
        dto.setType(TransactionType.EXPENSE);

        Category savedCategory = new Category("Спорт", TransactionType.EXPENSE);
        given(categoryRepository.save(any(Category.class))).willReturn(savedCategory);

        Category result = categoryService.save(dto);

        // ArgumentCaptor перехватывает аргумент, переданный в repository.save()
        // Позволяет проверить не только факт вызова, но и СОДЕРЖИМОЕ переданного объекта
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());

        Category captured = captor.getValue();
        assertThat(captured.getName()).isEqualTo("Спорт");
        assertThat(captured.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result).isSameAs(savedCategory);
    }

    // --- deleteById ---

    @Test
    void deleteById_whenExists_shouldDelete() {
        given(categoryRepository.existsById(1L)).willReturn(true);

        categoryService.deleteById(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteById_whenNotExists_shouldThrowResourceNotFoundException() {
        given(categoryRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> categoryService.deleteById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        // убеждаемся, что deleteById не вызывался — нечего удалять
        verify(categoryRepository, never()).deleteById(any());
    }
}
