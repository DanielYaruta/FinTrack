package com.fintrack.service;

import com.fintrack.dto.FinancialMetrics;
import com.fintrack.dto.MonthlyMetrics;
import com.fintrack.model.Category;
import com.fintrack.model.Transaction;
import com.fintrack.model.TransactionType;
import com.fintrack.model.User;
import com.fintrack.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    // --- getMetrics ---

    @Test
    void getMetrics_shouldCalculateBalanceCorrectly() {
        given(transactionRepository.sumByUserIdAndType(1L, TransactionType.INCOME))
                .willReturn(BigDecimal.valueOf(100_000));
        given(transactionRepository.sumByUserIdAndType(1L, TransactionType.EXPENSE))
                .willReturn(BigDecimal.valueOf(60_000));
        given(transactionRepository.countByUserId(1L)).willReturn(15L);

        FinancialMetrics metrics = analyticsService.getMetrics(1L);

        assertThat(metrics.totalIncome()).isEqualByComparingTo("100000");
        assertThat(metrics.totalExpense()).isEqualByComparingTo("60000");
        // balance = income - expense
        assertThat(metrics.balance()).isEqualByComparingTo("40000");
        assertThat(metrics.transactionCount()).isEqualTo(15L);
    }

    @Test
    void getMetrics_whenNoTransactions_shouldReturnZeroesInsteadOfNull() {
        // SQL SUM() возвращает NULL, если строк нет — сервис должен заменить NULL на 0
        given(transactionRepository.sumByUserIdAndType(any(), any())).willReturn(null);
        given(transactionRepository.countByUserId(any())).willReturn(0L);

        FinancialMetrics metrics = analyticsService.getMetrics(1L);

        // isEqualByComparingTo сравнивает значения, игнорируя scale (0 vs 0.00)
        assertThat(metrics.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.transactionCount()).isZero();
    }

    // --- getMetricsForPeriod ---

    @Test
    void getMetricsForPeriod_shouldAggregateTransactionsInPeriod() {
        LocalDate from = LocalDate.of(2024, 6, 1);
        LocalDate to   = LocalDate.of(2024, 6, 30);

        List<Transaction> transactions = List.of(
                makeTransaction(BigDecimal.valueOf(5_000), TransactionType.INCOME),
                makeTransaction(BigDecimal.valueOf(3_000), TransactionType.INCOME),
                makeTransaction(BigDecimal.valueOf(2_000), TransactionType.EXPENSE)
        );

        given(transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(1L, from, to))
                .willReturn(transactions);

        FinancialMetrics metrics = analyticsService.getMetricsForPeriod(1L, from, to);

        assertThat(metrics.totalIncome()).isEqualByComparingTo("8000");
        assertThat(metrics.totalExpense()).isEqualByComparingTo("2000");
        assertThat(metrics.balance()).isEqualByComparingTo("6000");
        assertThat(metrics.transactionCount()).isEqualTo(3L);
    }

    @Test
    void getMetricsForPeriod_whenEmpty_shouldReturnAllZeroes() {
        given(transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .willReturn(List.of());

        FinancialMetrics metrics = analyticsService.getMetricsForPeriod(1L,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThat(metrics.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.transactionCount()).isZero();
    }

    // --- getMonthlyTrend ---

    @Test
    void getMonthlyTrend_shouldReturnExactlyNMonths() {
        given(transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(List.of());

        List<MonthlyMetrics> trend = analyticsService.getMonthlyTrend(1L, 6);

        // Всегда N элементов — пустые месяцы включены с нулями
        assertThat(trend).hasSize(6);
    }

    @Test
    void getMonthlyTrend_shouldGroupTransactionsByMonth() {
        // Используем даты в ТЕКУЩИХ месяцах, иначе getMonthlyTrend()
        // строит диапазон от LocalDate.now()-5мес до now() и сопоставляет
        // результаты по ключу "yyyy-MM" — 2024-хх не совпадёт с 2026-хх.
        LocalDate thisMonth  = LocalDate.now().withDayOfMonth(15);
        LocalDate lastMonth  = LocalDate.now().minusMonths(2).withDayOfMonth(10);
        List<Transaction> txs = List.of(
                makeTransactionOn(BigDecimal.valueOf(10_000), TransactionType.INCOME,  thisMonth),
                makeTransactionOn(BigDecimal.valueOf(4_000),  TransactionType.EXPENSE, thisMonth),
                makeTransactionOn(BigDecimal.valueOf(8_000),  TransactionType.INCOME,  lastMonth)
        );

        given(transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(txs);

        List<MonthlyMetrics> trend = analyticsService.getMonthlyTrend(1L, 6);

        assertThat(trend).hasSize(6);
        // Хотя бы один месяц содержит ненулевые суммы
        boolean anyNonZero = trend.stream()
                .anyMatch(m -> m.income().compareTo(BigDecimal.ZERO) > 0
                        || m.expense().compareTo(BigDecimal.ZERO) > 0);
        assertThat(anyNonZero).isTrue();
    }

    // --- sumByType (package-private, тестируем напрямую) ---

    @Test
    void sumByType_shouldSumOnlyMatchingType() {
        List<Transaction> txs = List.of(
                makeTransaction(BigDecimal.valueOf(1000), TransactionType.INCOME),
                makeTransaction(BigDecimal.valueOf(500),  TransactionType.INCOME),
                makeTransaction(BigDecimal.valueOf(300),  TransactionType.EXPENSE)
        );

        BigDecimal incomeSum = analyticsService.sumByType(txs, TransactionType.INCOME);

        assertThat(incomeSum).isEqualByComparingTo("1500");
    }

    // --- Вспомогательные методы ---

    private Transaction makeTransaction(BigDecimal amount, TransactionType type) {
        return new Transaction(amount, type, null, LocalDate.of(2024, 6, 1), null, null);
    }

    private Transaction makeTransactionOn(BigDecimal amount, TransactionType type, LocalDate date) {
        return new Transaction(amount, type, null, date, null, null);
    }
}
