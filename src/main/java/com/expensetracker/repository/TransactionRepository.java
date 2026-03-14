package com.expensetracker.repository;

import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.Transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByDateDesc(Long userId);

    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    List<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    // Total income or expense for a user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(
            @Param("userId") Long userId,
            @Param("type") TransactionType type);

    // Monthly spend per category (for budget enforcement)
    @Query("SELECT t.category as category, COALESCE(SUM(t.amount), 0) as total " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "  AND t.date >= :startDate AND t.date <= :endDate " +
           "GROUP BY t.category")
    List<Map<String, Object>> sumExpenseByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Spend for a specific category in a date range
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "  AND t.category = :category " +
           "  AND t.date >= :startDate AND t.date <= :endDate")
    BigDecimal sumExpenseByCategoryAndDateRange(
            @Param("userId")    Long userId,
            @Param("category")  String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate);
}
