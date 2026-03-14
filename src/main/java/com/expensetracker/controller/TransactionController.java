package com.expensetracker.controller;

import com.expensetracker.dto.TransactionDTO;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.Transaction.TransactionType;
import com.expensetracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000",
                         "https://smart-expense-tracker.onrender.com"})
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // ── Add Transaction (with balance & budget guard) ─────
    @PostMapping
    public ResponseEntity<Object> addTransaction(@Valid @RequestBody TransactionDTO dto) {
        try {
            // If expense — check user has enough balance
            if (dto.getType() == TransactionType.EXPENSE) {
                BigDecimal balance = transactionService.getCurrentBalance(dto.getUserId());
                if (dto.getAmount().compareTo(balance) > 0) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "Insufficient balance! Your balance is ₹" +
                            balance.toPlainString() + " but you are trying to add ₹" +
                            dto.getAmount().toPlainString() +
                            ". Please add income first.");
                    err.put("balance", balance);
                    err.put("required", dto.getAmount());
                    return ResponseEntity.badRequest().body(err);
                }
            }
            Transaction saved = transactionService.addTransaction(dto);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ── Get All Transactions ──────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
    }

    // ── Filter by Type ────────────────────────────────────
    @GetMapping("/user/{userId}/type")
    public ResponseEntity<List<Transaction>> getByType(
            @PathVariable Long userId,
            @RequestParam TransactionType type) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(userId, type));
    }

    // ── Filter by Date Range ──────────────────────────────
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<Transaction>> getByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
                transactionService.getTransactionsByDateRange(userId, startDate, endDate));
    }

    // ── Update ────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO dto) {
        try {
            Transaction updated = transactionService.updateTransaction(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ── Delete ────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            transactionService.deleteTransaction(id);
            response.put("message", "Transaction deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    // ── Overall Summary ───────────────────────────────────
    @GetMapping("/summary/{userId}")
    public ResponseEntity<Map<String, BigDecimal>> getSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getSummary(userId));
    }

    // ── Monthly Category Summary ──────────────────────────
    // GET /api/transactions/summary/{userId}/monthly?year=2025&month=3
    @GetMapping("/summary/{userId}/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlySummary(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        if (year == 0 || month == 0) {
            LocalDate now = LocalDate.now();
            year  = now.getYear();
            month = now.getMonthValue();
        }
        return ResponseEntity.ok(transactionService.getMonthlyCategorySummary(userId, year, month));
    }

    // ── Current Balance ───────────────────────────────────
    @GetMapping("/balance/{userId}")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable Long userId) {
        Map<String, BigDecimal> res = new HashMap<>();
        res.put("balance", transactionService.getCurrentBalance(userId));
        return ResponseEntity.ok(res);
    }
}
