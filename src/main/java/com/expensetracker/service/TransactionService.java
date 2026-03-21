package com.expensetracker.service;

import com.expensetracker.dto.TransactionDTO;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.Transaction.TransactionType;
import com.expensetracker.entity.User;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;

    public BigDecimal getCurrentBalance(Long userId) {
        BigDecimal income  = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME);
        BigDecimal expense = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE);
        income  = income  != null ? income  : BigDecimal.ZERO;
        expense = expense != null ? expense : BigDecimal.ZERO;
        return income.subtract(expense);
    }

    public Transaction addTransaction(TransactionDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUserId()));
        Transaction t = new Transaction();
        t.setUser(user);
        t.setType(dto.getType());
        t.setAmount(dto.getAmount());
        t.setCategory(dto.getCategory());
        t.setDescription(dto.getDescription());
        t.setDate(dto.getDate());
        return transactionRepository.save(t);
    }

    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<Transaction> getTransactionsByType(Long userId, TransactionType type) {
        return transactionRepository.findByUserIdAndType(userId, type);
    }

    public List<Transaction> getTransactionsByDateRange(Long userId, LocalDate start, LocalDate end) {
        return transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);
    }

    public Transaction updateTransaction(Long id, TransactionDTO dto) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        t.setType(dto.getType());
        t.setAmount(dto.getAmount());
        t.setCategory(dto.getCategory());
        t.setDescription(dto.getDescription());
        t.setDate(dto.getDate());
        return transactionRepository.save(t);
    }

    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id))
            throw new RuntimeException("Transaction not found: " + id);
        transactionRepository.deleteById(id);
    }

    public Map<String, BigDecimal> getSummary(Long userId) {
        BigDecimal income  = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME);
        BigDecimal expense = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE);
        income  = income  != null ? income  : BigDecimal.ZERO;
        expense = expense != null ? expense : BigDecimal.ZERO;
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("income",  income);
        summary.put("expense", expense);
        summary.put("balance", income.subtract(expense));
        return summary;
    }

    public Map<String, BigDecimal> getMonthlyCategorySummary(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<Map<String, Object>> rows = transactionRepository
                .sumExpenseByCategory(userId, ym.atDay(1), ym.atEndOfMonth());
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String cat = (String) row.get("category");
            Object total = row.get("total");
            if (cat != null && total != null)
                result.put(cat, new BigDecimal(total.toString()));
        }
        return result;
    }
}
