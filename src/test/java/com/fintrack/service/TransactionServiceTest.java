package com.fintrack.service;

import com.fintrack.dto.TransactionDto;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.model.TransactionType;
import com.fintrack.model.User;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

/**
 * Чистый юнит-тест без Spring-контекста.
 *
 * @ExtendWith(MockitoExtension.class) — подключает Mockito к JUnit 5.
 *   Без него @Mock и @InjectMocks не работают.
 *
 * @Mock — Mockito создаёт фиктивный объект; все методы по умолчанию возвращают null/0/empty.
 *   Мы настраиваем только то поведение, которое нужно конкретному тесту.
 *
 * @InjectMocks — Mockito создаёт реальный TransactionService и внедряет
 *   все @Mock-поля через конструктор (у нас) или сеттеры.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    // --- findAllByUserId ---

    @Test
    void findAllByUserId_shouldDelegateToRepository() {
        List<Transaction> expected = List.of(makeTransaction(1L, 1L));
        given(transactionRepository.findByUserIdOrderByDateDesc(1L)).willReturn(expected);

        List<Transaction> result = transactionService.findAllByUserId(1L);

        assertThat(result).isSameAs(expected);
        verify(transactionRepository).findByUserIdOrderByDateDesc(1L);
    }

    // --- findById ---

    @Test
    void findById_whenExists_shouldReturnTransaction() {
        Transaction tx = makeTransaction(42L, 1L);
        given(transactionRepository.findById(42L)).willReturn(Optional.of(tx));

        Transaction result = transactionService.findById(42L);

        assertThat(result).isSameAs(tx);
    }

    @Test
    void findById_whenNotExists_shouldThrowResourceNotFoundException() {
        given(transactionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- save ---

    @Test
    void save_shouldCreateAndPersistTransaction() {
        User user = makeUser(1L);
        Category category = new Category("Продукты", TransactionType.EXPENSE);

        given(userService.findById(1L)).willReturn(user);
        given(categoryRepository.findById(4L)).willReturn(Optional.of(category));

        Transaction saved = makeTransaction(10L, 1L);
        given(transactionRepository.save(any(Transaction.class))).willReturn(saved);

        TransactionDto dto = new TransactionDto();
        dto.setAmount(BigDecimal.valueOf(3500));
        dto.setType(TransactionType.EXPENSE);
        dto.setCategoryId(4L);
        dto.setDate(LocalDate.of(2024, 6, 15));
        dto.setDescription("Магазин");

        Transaction result = transactionService.save(dto, 1L);

        // Проверяем, что метод вызван ровно один раз с любым Transaction-объектом
        verify(transactionRepository).save(any(Transaction.class));
        assertThat(result).isSameAs(saved);
    }

    @Test
    void save_withNullCategoryId_shouldSaveWithoutCategory() {
        User user = makeUser(1L);
        given(userService.findById(1L)).willReturn(user);
        given(transactionRepository.save(any(Transaction.class))).willReturn(makeTransaction(11L, 1L));

        TransactionDto dto = new TransactionDto();
        dto.setAmount(BigDecimal.valueOf(1000));
        dto.setType(TransactionType.INCOME);
        dto.setCategoryId(null);   // категория не выбрана
        dto.setDate(LocalDate.of(2024, 6, 10));

        transactionService.save(dto, 1L);

        // CategoryRepository не должен был вызываться
        verify(categoryRepository, never()).findById(any());
        verify(transactionRepository).save(any(Transaction.class));
    }

    // --- deleteById ---

    @Test
    void deleteById_whenOwner_shouldDeleteTransaction() {
        Transaction tx = makeTransaction(5L, 1L);
        given(transactionRepository.findById(5L)).willReturn(Optional.of(tx));

        transactionService.deleteById(5L, 1L);

        verify(transactionRepository).delete(tx);
    }

    @Test
    void deleteById_whenNotOwner_shouldThrowSecurityException() {
        // Транзакция принадлежит пользователю 99, а удаляет пользователь 1
        Transaction tx = makeTransaction(5L, 99L);
        given(transactionRepository.findById(5L)).willReturn(Optional.of(tx));

        assertThatThrownBy(() -> transactionService.deleteById(5L, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Нет доступа");
    }

    @Test
    void deleteById_whenNotExists_shouldThrowResourceNotFoundException() {
        given(transactionRepository.findById(77L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteById(77L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- Вспомогательные методы ---

    /**
     * Создаёт Transaction с заданным id и пользователем.
     * ReflectionTestUtils.setField позволяет установить private-поле без сеттера.
     * В продакшн-коде id устанавливает JPA после INSERT.
     */
    private Transaction makeTransaction(Long txId, Long userId) {
        User user = makeUser(userId);
        Transaction tx = new Transaction(
                BigDecimal.valueOf(5000),
                TransactionType.EXPENSE,
                null,
                LocalDate.of(2024, 6, 1),
                "test",
                user
        );
        ReflectionTestUtils.setField(tx, "id", txId);
        return tx;
    }

    private User makeUser(Long userId) {
        User user = new User("Test User", "test@example.com", LocalDate.now());
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
