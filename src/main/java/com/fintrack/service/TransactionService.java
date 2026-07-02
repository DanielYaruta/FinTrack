package com.fintrack.service;

import com.fintrack.dto.TransactionDto;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.model.User;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              UserService userService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    public List<Transaction> findAllByUserId(Long userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to) {
        return transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, from, to);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Транзакция", id));
    }

    public Transaction findByIdAndUserId(Long id, Long userId) {
        Transaction transaction = findById(id);
        // 404 вместо 403: не раскрываем факт существования чужой записи
        if (!transaction.getUser().getId().equals(userId)) {
            throw ResourceNotFoundException.of("Транзакция", id);
        }
        return transaction;
    }

    @Transactional
    public Transaction save(TransactionDto dto, Long userId) {
        User user = userService.findById(userId);
        Category category = resolveCategory(dto.getCategoryId());

        Transaction transaction = new Transaction(
                dto.getAmount(),
                dto.getType(),
                category,
                dto.getDate(),
                dto.getDescription(),
                user
        );
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction update(Long id, TransactionDto dto, Long userId) {
        Transaction transaction = findByIdAndUserId(id, userId);

        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setCategory(resolveCategory(dto.getCategoryId()));
        transaction.setDate(dto.getDate());
        transaction.setDescription(dto.getDescription());
        // Hibernate сделает UPDATE автоматически при закрытии транзакции
        return transaction;
    }

    @Transactional
    public void deleteById(Long id, Long userId) {
        transactionRepository.delete(findByIdAndUserId(id, userId));
    }

    // --- Вспомогательные методы ---

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Категория", categoryId));
    }
}
