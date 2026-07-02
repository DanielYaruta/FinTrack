package com.fintrack.repository;

import com.fintrack.model.Transaction;
import com.fintrack.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Все транзакции пользователя, отсортированные по дате (новые первые)
    List<Transaction> findByUserIdOrderByDateDesc(Long userId);

    // Транзакции пользователя по типу (INCOME или EXPENSE)
    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    // Транзакции за период
    List<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(
            Long userId, LocalDate from, LocalDate to);

    /**
     * @Query — JPQL-запрос (Java Persistence Query Language).
     * Похож на SQL, но работает с именами классов и полей Java, а не таблиц.
     * "t.user.id" — JPA сам переводит это в JOIN с таблицей users.
     *
     * SUM возвращает null, если транзакций нет — поэтому BigDecimal, а не примитив.
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);
}
